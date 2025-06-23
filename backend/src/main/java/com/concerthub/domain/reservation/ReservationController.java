package com.concerthub.domain.reservation;

import com.concerthub.domain.payment.service.PaymentService;
import com.concerthub.domain.reservation.dto.request.ReservationConfirmRequest;
import com.concerthub.domain.reservation.dto.request.ReservationCreateRequest;
import com.concerthub.domain.reservation.dto.response.ReservationResponse;
import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.service.ReservationService;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import com.concerthub.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody ReservationCreateRequest request) {
        Reservation reservation = reservationService.createReservation(
                request.getEventId(),
                request.getSeatId(),
                request.getUserId()
        );

        return ApiResponse.success(ReservationResponse.from(reservation),
                "예약이 생성되었습니다. 15분 이내에 결제를 완료해주세요.");
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ApiResponse.success(ReservationResponse.from(reservation));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<ReservationResponse>> getUserReservations(@PathVariable Long userId) {
        List<Reservation> reservations = reservationService.getUserReservations(userId);
        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<ReservationResponse> confirmReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationConfirmRequest request) {

        // 예약 정보 조회
        Reservation reservation = reservationService.getReservationById(id);

        // 결제 처리 (모킹)
        PaymentService.PaymentResult paymentResult = paymentService.processPayment(
                id,
                reservation.getTotalPrice(),
                reservation.getUser().getEmail()
        );

        if (!paymentResult.isSuccess()) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, paymentResult.getErrorMessage());
        }

        // 예약 확정
        Reservation confirmedReservation = reservationService.confirmReservation(id, paymentResult.getPaymentId());

        return ApiResponse.success(ReservationResponse.from(confirmedReservation),
                "결제가 완료되어 예약이 확정되었습니다.");
    }

    @DeleteMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancelReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);

        // 확정된 예약이고 결제ID가 있다면 결제 취소 처리
        if (reservation.isConfirmed() && reservation.getPaymentId() != null) {
            boolean cancelSuccess = paymentService.cancelPayment(reservation.getPaymentId());
            if (!cancelSuccess) {
                throw new BusinessException(ErrorCode.PAYMENT_FAILED, "결제 취소 처리에 실패했습니다.");
            }
        }

        // 예약 취소
        Reservation cancelledReservation = reservationService.cancelReservation(id);

        return ApiResponse.success(ReservationResponse.from(cancelledReservation),
                "예약이 취소되었습니다.");
    }
}