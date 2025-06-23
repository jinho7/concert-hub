package com.concerthub.domain.admin.controller;

import com.concerthub.domain.admin.dto.request.ReservationSearchRequest;
import com.concerthub.domain.admin.dto.response.AdminReservationResponse;
import com.concerthub.domain.admin.dto.response.ReservationStatsResponse;
import com.concerthub.domain.admin.service.AdminReservationService;
import com.concerthub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    /**
     * 전체 예약 내역 조회 (필터링 + 페이지네이션)
     */
    @GetMapping
    public ApiResponse<Page<AdminReservationResponse>> getAllReservations(
            @ModelAttribute ReservationSearchRequest request) {
        
        Page<AdminReservationResponse> reservations = adminReservationService.searchReservations(request);
        return ApiResponse.onSuccess(reservations);
    }

    /**
     * 이벤트별 예약 현황 조회
     */
    @GetMapping("/events/{eventId}")
    public ApiResponse<Page<AdminReservationResponse>> getEventReservations(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<AdminReservationResponse> reservations = 
                adminReservationService.getEventReservations(eventId, page, size);
        return ApiResponse.onSuccess(reservations);
    }

    /**
     * 예약 통계 조회
     */
    @GetMapping("/stats")
    public ApiResponse<ReservationStatsResponse> getReservationStats() {
        ReservationStatsResponse stats = adminReservationService.getReservationStats();
        return ApiResponse.onSuccess(stats);
    }
}
