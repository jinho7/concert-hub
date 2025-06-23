package com.concerthub.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    @NotNull(message = "이벤트 ID는 필수입니다.")
    private Long eventId;

    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
}