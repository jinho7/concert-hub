package com.concerthub.domain.reservation.repository;

import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 사용자별 예약 목록
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 상태별 예약 조회
    List<Reservation> findByStatus(ReservationStatus status);

    // 만료된 예약 조회
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.expiresAt < :currentTime")
    List<Reservation> findExpiredReservations(@Param("status") ReservationStatus status,
                                              @Param("currentTime") LocalDateTime currentTime);

    // 특정 좌석의 활성 예약 확인 (PENDING, CONFIRMED 상태)
    @Query("SELECT r FROM Reservation r WHERE r.seat.id = :seatId AND r.status IN (:statuses)")
    Optional<Reservation> findActiveBySeatId(@Param("seatId") Long seatId,
                                             @Param("statuses") List<ReservationStatus> statuses);

    // 비관적 락으로 예약 조회 (동시성 제어용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdWithLock(@Param("id") Long id);

    // 이벤트별 예약 수 카운트
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.event.id = :eventId AND r.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ReservationStatus status);
}