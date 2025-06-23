package com.concerthub.domain.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class PaymentService {

    /**
     * 결제 처리 모킹
     * 실제로는 외부 결제 API 호출 [pg사 연결 시 사업자 등록 필요]
     */
    public PaymentResult processPayment(Long reservationId, Integer amount, String userEmail) {
        log.info("결제 처리 시작: 예약ID={}, 금액={}, 사용자={}", reservationId, amount, userEmail);

        // 결제 처리 시뮬레이션 (1-3초 소요)
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5% 확률로 결제 실패 시뮬레이션
        boolean isSuccess = ThreadLocalRandom.current().nextDouble() > 0.05;

        if (isSuccess) {
            String paymentId = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
            log.info("결제 성공: 결제ID={}", paymentId);

            return PaymentResult.success(paymentId, amount);
        } else {
            log.warn("결제 실패: 예약ID={}", reservationId);
            return PaymentResult.failure("결제 승인이 거절되었습니다. 카드사에 문의하세요.");
        }
    }

    /**
     * 결제 취소 모킹
     */
    public boolean cancelPayment(String paymentId) {
        log.info("결제 취소 처리: 결제ID={}", paymentId);

        // 취소 처리 시뮬레이션
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 95% 확률로 취소 성공
        boolean isSuccess = ThreadLocalRandom.current().nextDouble() > 0.05;

        if (isSuccess) {
            log.info("결제 취소 성공: 결제ID={}", paymentId);
        } else {
            log.warn("결제 취소 실패: 결제ID={}", paymentId);
        }

        return isSuccess;
    }

    public static class PaymentResult {
        private final boolean success;
        private final String paymentId;
        private final Integer amount;
        private final String errorMessage;

        private PaymentResult(boolean success, String paymentId, Integer amount, String errorMessage) {
            this.success = success;
            this.paymentId = paymentId;
            this.amount = amount;
            this.errorMessage = errorMessage;
        }

        public static PaymentResult success(String paymentId, Integer amount) {
            return new PaymentResult(true, paymentId, amount, null);
        }

        public static PaymentResult failure(String errorMessage) {
            return new PaymentResult(false, null, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public String getPaymentId() { return paymentId; }
        public Integer getAmount() { return amount; }
        public String getErrorMessage() { return errorMessage; }
    }
}