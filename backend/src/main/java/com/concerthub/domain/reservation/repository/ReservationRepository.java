package com.concerthub.domain.reservation.repository;

import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    // 관리자용 예약 검색 (복합 조건)
    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.event e " +
           "JOIN FETCH r.seat s " +
           "JOIN FETCH r.user u " +
           "WHERE (:eventId IS NULL OR r.event.id = :eventId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR r.createdAt <= :endDate) " +
           "AND (:userEmail IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))) " +
           "AND (:userName IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :userName, '%')))")
    Page<Reservation> findReservationsWithFilters(
            @Param("eventId") Long eventId,
            @Param("status") ReservationStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userEmail") String userEmail,
            @Param("userName") String userName,
            Pageable pageable);

    // 예약 통계 - 상태별 개수
    @Query("SELECT r.status, COUNT(r) FROM Reservation r GROUP BY r.status")
    List<Object[]> countReservationsByStatus();

    // 예약 통계 - 상태별 매출
    @Query("SELECT r.status, COALESCE(SUM(CAST(r.totalPrice AS java.math.BigDecimal)), 0) FROM Reservation r GROUP BY r.status")
    List<Object[]> sumRevenueByStatus();

    // 기간별 예약 통계
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.createdAt >= :startDate AND r.createdAt < :endDate")
    Long countReservationsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 기간별 매출 통계
    @Query("SELECT COALESCE(SUM(CAST(r.totalPrice AS java.math.BigDecimal)), 0) FROM Reservation r " +
           "WHERE r.status = 'CONFIRMED' AND r.createdAt >= :startDate AND r.createdAt < :endDate")
    BigDecimal sumConfirmedRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 이벤트별 예약 현황 (페이지네이션)
    @Query("SELECT r FROM Reservation r " +
           "JOIN FETCH r.seat s " +
           "JOIN FETCH r.user u " +
           "WHERE r.event.id = :eventId")
    Page<Reservation> findByEventIdWithDetails(@Param("eventId") Long eventId, Pageable pageable);
}