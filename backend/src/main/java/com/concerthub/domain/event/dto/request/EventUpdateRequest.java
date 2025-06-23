package com.concerthub.domain.event.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EventUpdateRequest {

    @Size(max = 100, message = "이벤트 제목은 100자를 초과할 수 없습니다.")
    private String title;

    @Size(max = 1000, message = "이벤트 설명은 1000자를 초과할 수 없습니다.")
    private String description;

    @Size(max = 100, message = "장소는 100자를 초과할 수 없습니다.")
    private String venue;

    @Future(message = "이벤트 일시는 현재 시간 이후여야 합니다.")
    private LocalDateTime eventDateTime;

    @Min(value = 1, message = "총 좌석 수는 1 이상이어야 합니다.")
    @Max(value = 100000, message = "총 좌석 수는 100,000을 초과할 수 없습니다.")
    private Integer totalSeats;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;
}
