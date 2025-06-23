package com.concerthub.domain.reservation.entity;

import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.reservation.entity.status.ReservationStatus;
import com.concerthub.domain.seat.entity.Seat;
import com.concerthub.domain.user.entity.User;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column
    private LocalDateTime expiresAt; // 예약 만료 시간

    @Column
    private String paymentId; // 결제 ID (모킹용)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Reservation(Event event, Seat seat, User user, Integer totalPrice) {
        this.event = event;
        this.seat = seat;
        this.user = user;
        this.totalPrice = totalPrice;
        this.status = ReservationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusMinutes(15); // 15분 후 만료
    }

    // 비즈니스 로직
    public void confirmReservation(String paymentId) {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("대기 중인 예약만 확정할 수 있습니다.");
        }
        if (isExpired()) {
            throw new IllegalStateException("만료된 예약은 확정할 수 없습니다.");
        }

        this.status = ReservationStatus.CONFIRMED;
        this.paymentId = paymentId;
        this.expiresAt = null; // 확정된 예약은 만료시간 제거
    }

    public void cancelReservation() {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }
        if (this.status == ReservationStatus.EXPIRED) {
            throw new IllegalStateException("만료된 예약은 취소할 수 없습니다.");
        }

        this.status = ReservationStatus.CANCELLED;
        this.expiresAt = null;
    }

    public void expireReservation() {
        if (this.status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.EXPIRED;
        }
    }

    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isPending() {
        return this.status == ReservationStatus.PENDING;
    }

    public boolean isConfirmed() {
        return this.status == ReservationStatus.CONFIRMED;
    }

    public boolean canBeCancelled() {
        return this.status == ReservationStatus.PENDING || this.status == ReservationStatus.CONFIRMED;
    }

    public long getMinutesUntilExpiry() {
        if (this.expiresAt == null) {
            return -1;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.expiresAt).toMinutes();
    }
}