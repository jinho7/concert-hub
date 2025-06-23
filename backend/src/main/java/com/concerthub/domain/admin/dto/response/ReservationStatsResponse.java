package com.concerthub.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReservationStatsResponse {

    // 예약 수 통계
    private Long totalReservations;
    private Long pendingReservations;
    private Long confirmedReservations;
    private Long cancelledReservations;
    private Long expiredReservations;
    
    // 매출 통계
    private BigDecimal totalRevenue;
    private BigDecimal confirmedRevenue;
    private BigDecimal pendingRevenue;
    
    // 기간별 통계
    private Long todayReservations;
    private Long thisWeekReservations;
    private Long thisMonthReservations;
    
    // 매출 기간별 통계
    private BigDecimal todayRevenue;
    private BigDecimal thisWeekRevenue;
    private BigDecimal thisMonthRevenue;
}
