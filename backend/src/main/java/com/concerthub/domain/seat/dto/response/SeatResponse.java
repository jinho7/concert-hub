package com.concerthub.domain.seat.dto.response;

import com.concerthub.domain.seat.entity.Seat;
import com.concerthub.domain.seat.entity.status.SeatStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SeatResponse {

    private Long id;
    private Long eventId;
    private String seatRow;
    private String seatNumber;
    private String seatDisplay;
    private Integer price;
    private SeatStatus status;
    private LocalDateTime temporaryReservedAt;
    private boolean isExpired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SeatResponse from(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .eventId(seat.getEvent().getId())
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .seatDisplay(seat.getSeatDisplay())
                .price(seat.getPrice())
                .status(seat.getStatus())
                .temporaryReservedAt(seat.getTemporaryReservedAt())
                .isExpired(seat.isTemporaryReservationExpired())
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }
}