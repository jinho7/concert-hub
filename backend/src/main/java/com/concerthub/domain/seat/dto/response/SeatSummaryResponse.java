package com.concerthub.domain.seat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class SeatSummaryResponse {

    private Long eventId;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer temporaryReservedSeats;
    private Integer reservedSeats;
    private Integer blockedSeats;
    private Map<String, Integer> priceRange; // min, max 가격

    public static SeatSummaryResponse of(Long eventId,
                                         Map<String, Integer> seatCounts,
                                         Integer minPrice,
                                         Integer maxPrice) {
        return SeatSummaryResponse.builder()
                .eventId(eventId)
                .totalSeats(seatCounts.values().stream().mapToInt(Integer::intValue).sum())
                .availableSeats(seatCounts.getOrDefault("AVAILABLE", 0))
                .temporaryReservedSeats(seatCounts.getOrDefault("TEMPORARILY_RESERVED", 0))
                .reservedSeats(seatCounts.getOrDefault("RESERVED", 0))
                .blockedSeats(seatCounts.getOrDefault("BLOCKED", 0))
                .priceRange(Map.of("min", minPrice, "max", maxPrice))
                .build();
    }
}