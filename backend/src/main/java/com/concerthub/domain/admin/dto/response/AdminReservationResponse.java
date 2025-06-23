package com.concerthub.domain.admin.dto.response;

import com.concerthub.domain.reservation.entity.Reservation;
import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReservationResponse {

    private Long id;
    private ReservationStatus status;
    private Integer totalPrice;
    private String paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    // 이벤트 정보
    private Long eventId;
    private String eventTitle;
    private String eventVenue;
    private LocalDateTime eventDateTime;
    
    // 좌석 정보
    private Long seatId;
    private String seatDisplay;
    private Integer seatPrice;
    
    // 사용자 정보
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;

    public static AdminReservationResponse from(Reservation reservation) {
        return AdminReservationResponse.builder()
                .id(reservation.getId())
                .status(reservation.getStatus())
                .totalPrice(reservation.getTotalPrice())
                .paymentId(reservation.getPaymentId())
                .createdAt(reservation.getCreatedAt())
                .expiresAt(reservation.getExpiresAt())
                .eventId(reservation.getEvent().getId())
                .eventTitle(reservation.getEvent().getTitle())
                .eventVenue(reservation.getEvent().getVenue())
                .eventDateTime(reservation.getEvent().getEventDateTime())
                .seatId(reservation.getSeat().getId())
                .seatDisplay(reservation.getSeat().getSeatDisplay())
                .seatPrice(reservation.getSeat().getPrice())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getName())
                .userEmail(reservation.getUser().getEmail())
                .userPhoneNumber(reservation.getUser().getPhoneNumber())
                .build();
    }
}
