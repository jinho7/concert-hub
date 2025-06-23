package com.concerthub.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationConfirmRequest {

    @NotBlank(message = "결제 ID는 필수입니다.")
    private String paymentId;
}