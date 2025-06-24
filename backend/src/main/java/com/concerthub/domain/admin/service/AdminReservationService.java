package com.concerthub.domain.admin.service;

import com.concerthub.domain.admin.dto.request.ReservationSearchRequest;
import com.concerthub.domain.admin.dto.response.AdminReservationResponse;
import com.concerthub.domain.admin.dto.response.ReservationStatsResponse;
import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import com.concerthub.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;

    /**
     * 관리자용 예약 검색 (필터링 + 페이지네이션)
     */
    public Page<AdminReservationResponse> searchReservations(ReservationSearchRequest request) {
        // 정렬 설정
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 검색 실행
        Page<Reservation> reservationPage = reservationRepository.findReservationsWithFilters(
                request.getEventId(),
                request.getStatus(),
                request.getStartDate(),
                request.getEndDate(),
                request.getUserEmail(),
                request.getUserName(),
                pageable
        );

        // DTO 변환
        return reservationPage.map(AdminReservationResponse::from);
    }

    /**
     * 이벤트별 예약 현황 조회
     */
    public Page<AdminReservationResponse> getEventReservations(Long eventId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reservation> reservationPage = reservationRepository.findByEventIdWithDetails(eventId, pageable);
        return reservationPage.map(AdminReservationResponse::from);
    }

    /**
     * 예약 통계 조회
     */
    public ReservationStatsResponse getReservationStats() {
        // 상태별 예약 수 통계
        Map<ReservationStatus, Long> reservationCounts = getReservationCountsByStatus();
        
        // 상태별 매출 통계
        Map<ReservationStatus, BigDecimal> revenueByStatus = getRevenueByStatus();
        
        // 기간별 통계
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.with(LocalTime.MIN);
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusMonths(1);

        return ReservationStatsResponse.builder()
                // 예약 수 통계
                .totalReservations(reservationCounts.values().stream().mapToLong(Long::longValue).sum())
                .pendingReservations(reservationCounts.getOrDefault(ReservationStatus.PENDING, 0L))
                .confirmedReservations(reservationCounts.getOrDefault(ReservationStatus.CONFIRMED, 0L))
                .cancelledReservations(reservationCounts.getOrDefault(ReservationStatus.CANCELLED, 0L))
                .expiredReservations(reservationCounts.getOrDefault(ReservationStatus.EXPIRED, 0L))
                // 매출 통계
                .totalRevenue(revenueByStatus.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .confirmedRevenue(revenueByStatus.getOrDefault(ReservationStatus.CONFIRMED, BigDecimal.ZERO))
                .pendingRevenue(revenueByStatus.getOrDefault(ReservationStatus.PENDING, BigDecimal.ZERO))
                // 기간별 예약 수
                .todayReservations(reservationRepository.countReservationsByDateRange(todayStart, now))
                .thisWeekReservations(reservationRepository.countReservationsByDateRange(weekStart, now))
                .thisMonthReservations(reservationRepository.countReservationsByDateRange(monthStart, now))
                // 기간별 매출
                .todayRevenue(reservationRepository.sumConfirmedRevenueByDateRange(todayStart, now))
                .thisWeekRevenue(reservationRepository.sumConfirmedRevenueByDateRange(weekStart, now))
                .thisMonthRevenue(reservationRepository.sumConfirmedRevenueByDateRange(monthStart, now))
                .build();
    }

    private Map<ReservationStatus, Long> getReservationCountsByStatus() {
        List<Object[]> results = reservationRepository.countReservationsByStatus();
        Map<ReservationStatus, Long> counts = new HashMap<>();
        
        for (Object[] result : results) {
            ReservationStatus status = (ReservationStatus) result[0];
            Long count = (Long) result[1];
            counts.put(status, count);
        }
        
        return counts;
    }

    private Map<ReservationStatus, BigDecimal> getRevenueByStatus() {
        List<Object[]> results = reservationRepository.sumRevenueByStatus();
        Map<ReservationStatus, BigDecimal> revenue = new HashMap<>();
        
        for (Object[] result : results) {
            ReservationStatus status = (ReservationStatus) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            revenue.put(status, amount != null ? amount : BigDecimal.ZERO);
        }
        
        return revenue;
    }
}
