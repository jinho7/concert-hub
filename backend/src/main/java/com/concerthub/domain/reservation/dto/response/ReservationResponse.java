package com.concerthub.domain.reservation.dto.response;

import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {

    private Long id;
    private EventInfo event;
    private SeatInfo seat;
    private UserInfo user;
    private ReservationStatus status;
    private Integer totalPrice;
    private LocalDateTime expiresAt;
    private Long minutesUntilExpiry;
    private String paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class EventInfo {
        private Long id;
        private String title;
        private String venue;
        private LocalDateTime eventDateTime;
    }

    @Getter
    @Builder
    public static class SeatInfo {
        private Long id;
        private String seatRow;
        private String seatNumber;
        private String seatDisplay;
        private Integer price;
    }

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
    }

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .event(EventInfo.builder()
                        .id(reservation.getEvent().getId())
                        .title(reservation.getEvent().getTitle())
                        .venue(reservation.getEvent().getVenue())
                        .eventDateTime(reservation.getEvent().getEventDateTime())
                        .build())
                .seat(SeatInfo.builder()
                        .id(reservation.getSeat().getId())
                        .seatRow(reservation.getSeat().getSeatRow())
                        .seatNumber(reservation.getSeat().getSeatNumber())
                        .seatDisplay(reservation.getSeat().getSeatDisplay())
                        .price(reservation.getSeat().getPrice())
                        .build())
                .user(UserInfo.builder()
                        .id(reservation.getUser().getId())
                        .name(reservation.getUser().getName())
                        .email(reservation.getUser().getEmail())
                        .build())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .expiresAt(reservation.getExpiresAt())
                .minutesUntilExpiry(reservation.getMinutesUntilExpiry())
                .paymentId(reservation.getPaymentId())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }
}