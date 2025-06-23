package com.concerthub.domain.reservation.service;

import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.event.service.EventService;
import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import com.concerthub.domain.reservation.repository.ReservationRepository;
import com.concerthub.domain.seat.entity.Seat;
import com.concerthub.domain.seat.entity.status.SeatStatus;
import com.concerthub.domain.seat.repository.SeatRepository;
import com.concerthub.domain.user.entity.User;
import com.concerthub.domain.user.repository.UserRepository;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Transactional
    public Reservation createReservation(Long eventId, Long seatId, Long userId) {
        // 1. 엔티티 조회
        Event event = eventService.getEvent(eventId);
        User user = getUserById(userId);

        // 2. 비관적 락으로 좌석 조회 (동시성 제어)
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        // 3. 예약 가능 여부 검증
        validateReservationPossible(seat, event);

        // 4. 기존 활성 예약 확인
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
        reservationRepository.findActiveBySeatId(seatId, activeStatuses)
                .ifPresent(existingReservation -> {
                    throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED,
                            "이미 예약된 좌석입니다.");
                });

        // 5. 좌석 임시 예약 처리
        try {
            seat.temporaryReserve();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE, e.getMessage());
        }

        // 6. 예약 생성
        Reservation reservation = Reservation.builder()
                .event(event)
                .seat(seat)
                .user(user)
                .totalPrice(seat.getPrice())
                .build();

        log.info("예약 생성 완료: 사용자={}, 좌석={}, 가격={}",
                userId, seat.getSeatDisplay(), seat.getPrice());

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation confirmReservation(Long reservationId, String paymentId) {
        // 1. 비관적 락으로 예약 조회
        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 2. 예약 확정 처리
        try {
            reservation.confirmReservation(paymentId);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_OPERATION, e.getMessage());
        }

        // 3. 좌석 상태 변경 (임시예약 → 확정예약)
        Seat seat = reservation.getSeat();
        try {
            seat.reserve();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_OPERATION, e.getMessage());
        }

        // 4. 이벤트 잔여석 감소
        Event event = reservation.getEvent();
        try {
            event.decreaseAvailableSeats();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_COUNT, e.getMessage());
        }

        log.info("예약 확정 완료: 예약ID={}, 결제ID={}", reservationId, paymentId);

        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        // 1. 예약 조회
        Reservation reservation = getReservationById(reservationId);

        // 2. 취소 가능 여부 확인
        if (!reservation.canBeCancelled()) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_OPERATION,
                    "취소할 수 없는 예약입니다.");
        }

        boolean wasConfirmed = reservation.isConfirmed();

        // 3. 예약 취소 처리
        try {
            reservation.cancelReservation();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_OPERATION, e.getMessage());
        }

        // 4. 좌석 상태 원복
        Seat seat = reservation.getSeat();
        seat.cancelReservation();

        // 5. 확정된 예약이었다면 이벤트 잔여석 증가
        if (wasConfirmed) {
            Event event = reservation.getEvent();
            event.increaseAvailableSeats();
        }

        log.info("예약 취소 완료: 예약ID={}, 확정예약여부={}", reservationId, wasConfirmed);

        return reservation;
    }

    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public List<Reservation> getUserReservations(Long userId) {
        getUserById(userId); // 사용자 존재 확인
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository
                .findExpiredReservations(ReservationStatus.PENDING, now);

        for (Reservation reservation : expiredReservations) {
            log.info("만료된 예약 정리: 예약ID={}", reservation.getId());

            // 예약 만료 처리
            reservation.expireReservation();

            // 좌석 상태 원복
            reservation.getSeat().cancelReservation();
        }

        if (!expiredReservations.isEmpty()) {
            log.info("만료된 예약 {}건 정리 완료", expiredReservations.size());
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateReservationPossible(Seat seat, Event event) {
        // 좌석 예약 가능 여부
        if (!seat.isAvailable()) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE,
                    "예약할 수 없는 좌석입니다. 현재 상태: " + seat.getStatus());
        }

        // 이벤트 예약 가능 여부
        if (event.getStatus() != com.concerthub.domain.event.entity.status.EventStatus.OPEN) {
            throw new BusinessException(ErrorCode.EVENT_NOT_AVAILABLE,
                    "예약할 수 없는 이벤트입니다.");
        }

        // 좌석이 해당 이벤트의 것인지 확인
        if (!seat.getEvent().getId().equals(event.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "해당 이벤트의 좌석이 아닙니다.");
        }
    }
}