package com.concerthub.global.scheduler;

import com.concerthub.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    /**
     * 매 1분마다 만료된 예약 정리
     */
    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void cleanupExpiredReservations() {
        try {
            reservationService.cleanupExpiredReservations();
        } catch (Exception e) {
            log.error("만료된 예약 정리 중 오류 발생", e);
        }
    }
}