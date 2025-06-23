package com.concerthub.domain.seat.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeatCreateRequest {

    @NotNull(message = "총 행 수는 필수입니다.")
    @Min(value = 1, message = "총 행 수는 1 이상이어야 합니다.")
    @Max(value = 50, message = "총 행 수는 50을 초과할 수 없습니다.")
    private Integer totalRows;

    @NotNull(message = "행당 좌석 수는 필수입니다.")
    @Min(value = 1, message = "행당 좌석 수는 1 이상이어야 합니다.")
    @Max(value = 100, message = "행당 좌석 수는 100을 초과할 수 없습니다.")
    private Integer seatsPerRow;

    @NotNull(message = "기본 가격은 필수입니다.")
    @Min(value = 0, message = "기본 가격은 0 이상이어야 합니다.")
    private Integer basePrice;
}