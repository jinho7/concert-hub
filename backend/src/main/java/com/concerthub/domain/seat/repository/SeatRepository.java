package com.concerthub.domain.seat.repository;

import com.concerthub.domain.seat.entity.Seat;
import com.concerthub.domain.seat.entity.status.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    // 이벤트별 좌석 조회
    List<Seat> findByEventIdOrderBySeatRowAscSeatNumberAsc(Long eventId);

    // 이벤트별 특정 상태 좌석 조회
    List<Seat> findByEventIdAndStatus(Long eventId, SeatStatus status);

    // 예약 가능한 좌석만 조회
    List<Seat> findByEventIdAndStatusOrderBySeatRowAscSeatNumberAsc(Long eventId, SeatStatus status);

    // 특정 좌석 조회 (이벤트ID + 행 + 번호)
    Optional<Seat> findByEventIdAndSeatRowAndSeatNumber(Long eventId, String seatRow, String seatNumber);

    // 만료된 임시 예약 좌석 조회
    @Query("SELECT s FROM Seat s WHERE s.status = :status AND s.temporaryReservedAt < :expiredTime")
    List<Seat> findExpiredTemporaryReservations(@Param("status") SeatStatus status,
                                                @Param("expiredTime") LocalDateTime expiredTime);

    // 이벤트별 상태별 좌석 수 카운트
    @Query("SELECT s.status, COUNT(s) FROM Seat s WHERE s.event.id = :eventId GROUP BY s.status")
    List<Object[]> countSeatsByStatus(@Param("eventId") Long eventId);

    // 특정 행의 좌석들 조회
    List<Seat> findByEventIdAndSeatRowOrderBySeatNumberAsc(Long eventId, String seatRow);
}