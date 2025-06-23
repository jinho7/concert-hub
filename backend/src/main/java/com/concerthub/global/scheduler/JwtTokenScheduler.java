package com.concerthub.global.scheduler;

import com.concerthub.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenScheduler {

    private final JwtUtil jwtUtil;

    /**
     * 매일 자정에 만료된 리프레시 토큰 정리
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {
        try {
            jwtUtil.cleanupExpiredTokens();
        } catch (Exception e) {
            log.error("만료된 토큰 정리 중 오류 발생", e);
        }
    }
}
