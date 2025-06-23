package com.concerthub.domain.payment.controller;

import com.concerthub.domain.payment.service.PaymentService;
import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.service.ReservationService;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import com.concerthub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    @PostMapping("/mock/{reservationId}")
    public ApiResponse<Map<String, Object>> mockPayment(@PathVariable Long reservationId) {
        // 예약 정보 조회
        Reservation reservation = reservationService.getReservationById(reservationId);

        // 결제 처리 모킹
        PaymentService.PaymentResult result = paymentService.processPayment(
                reservationId,
                reservation.getTotalPrice(),
                reservation.getUser().getEmail()
        );

        if (result.isSuccess()) {
            return ApiResponse.success(
                    Map.of(
                            "paymentId", result.getPaymentId(),
                            "amount", result.getAmount(),
                            "status", "SUCCESS"
                    ),
                    "결제가 성공적으로 처리되었습니다."
            );
        } else {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, result.getErrorMessage());
        }
    }

    @PostMapping("/cancel/{paymentId}")
    public ApiResponse<Map<String, Object>> cancelPayment(@PathVariable String paymentId) {
        boolean success = paymentService.cancelPayment(paymentId);

        if (success) {
            return ApiResponse.success(
                    Map.of("paymentId", paymentId, "status", "CANCELLED"),
                    "결제가 성공적으로 취소되었습니다."
            );
        } else {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "결제 취소에 실패했습니다.");
        }
    }
}