package com.concerthub.domain.event.dto.response;

import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.event.entity.status.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private String venue;
    private LocalDateTime eventDateTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer price;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDateTime(event.getEventDateTime())
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .price(event.getPrice())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}