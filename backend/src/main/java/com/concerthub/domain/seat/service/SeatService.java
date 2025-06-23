package com.concerthub.domain.seat.service;

import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.event.service.EventService;
import com.concerthub.domain.seat.entity.Seat;
import com.concerthub.domain.seat.entity.status.SeatStatus;
import com.concerthub.domain.seat.repository.SeatRepository;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventService eventService;

    @Transactional
    public List<Seat> createSeats(Long eventId, int totalRows, int seatsPerRow, Integer basePrice) {
        Event event = eventService.getEvent(eventId);

        List<Seat> seats = new ArrayList<>();

        for (int row = 1; row <= totalRows; row++) {
            String seatRow = String.valueOf((char) ('A' + row - 1)); // A, B, C...

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                // 좌석별 가격 차등 (앞자리 더 비쌈)
                Integer seatPrice = calculateSeatPrice(basePrice, row, totalRows);

                Seat seat = Seat.builder()
                        .event(event)
                        .seatRow(seatRow)
                        .seatNumber(String.valueOf(seatNum))
                        .price(seatPrice)
                        .build();

                seats.add(seat);
            }
        }

        return seatRepository.saveAll(seats);
    }

    public List<Seat> getEventSeats(Long eventId) {
        eventService.getEvent(eventId); // 이벤트 존재 확인
        return seatRepository.findByEventIdOrderBySeatRowAscSeatNumberAsc(eventId);
    }

    public List<Seat> getAvailableSeats(Long eventId) {
        eventService.getEvent(eventId); // 이벤트 존재 확인
        return seatRepository.findByEventIdAndStatusOrderBySeatRowAscSeatNumberAsc(eventId, SeatStatus.AVAILABLE);
    }

    public Seat getSeat(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
    }

    public Seat getSeat(Long eventId, String seatRow, String seatNumber) {
        return seatRepository.findByEventIdAndSeatRowAndSeatNumber(eventId, seatRow, seatNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
    }

    @Transactional
    public Seat temporaryReserveSeat(Long seatId) {
        Seat seat = getSeat(seatId);

        try {
            seat.temporaryReserve();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE, e.getMessage());
        }

        return seat;
    }

    @Transactional
    public Seat confirmReservation(Long seatId) {
        Seat seat = getSeat(seatId);

        try {
            seat.reserve();
            seat.getEvent().decreaseAvailableSeats();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_OPERATION, e.getMessage());
        }

        return seat;
    }

    @Transactional
    public Seat cancelReservation(Long seatId) {
        Seat seat = getSeat(seatId);

        boolean wasReserved = seat.getStatus() == SeatStatus.RESERVED;
        seat.cancelReservation();

        // 확정 예약이었다면 이벤트의 available 좌석 수 증가
        if (wasReserved) {
            seat.getEvent().increaseAvailableSeats();
        }

        return seat;
    }

    @Transactional
    public void cleanupExpiredTemporaryReservations() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(15);
        List<Seat> expiredSeats = seatRepository.findExpiredTemporaryReservations(
                SeatStatus.TEMPORARILY_RESERVED, expiredTime);

        expiredSeats.forEach(Seat::cancelReservation);
    }

    private Integer calculateSeatPrice(Integer basePrice, int row, int totalRows) {
        // 앞자리일수록 더 비쌈 (첫 번째 행이 가장 비쌈)
        double multiplier = 1.0 + (double) (totalRows - row) / totalRows * 0.5;
        return (int) (basePrice * multiplier);
    }
}