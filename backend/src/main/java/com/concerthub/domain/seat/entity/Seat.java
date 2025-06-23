package com.concerthub.domain.seat.entity;

import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.seat.entity.status.SeatStatus;
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
@Table(name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "seat_row", "seat_number"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 5)
    private String seatRow;      // A, B, C 등

    @Column(nullable = false, length = 10)
    private String seatNumber;   // 1, 2, 3 등

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column
    private LocalDateTime temporaryReservedAt; // 임시 예약 시작 시간

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Seat(Event event, String seatRow, String seatNumber, Integer price) {
        this.event = event;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    // 비즈니스 로직
    public void temporaryReserve() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("예약 가능한 좌석이 아닙니다.");
        }
        this.status = SeatStatus.TEMPORARILY_RESERVED;
        this.temporaryReservedAt = LocalDateTime.now();
    }

    public void reserve() {
        if (this.status != SeatStatus.TEMPORARILY_RESERVED) {
            throw new IllegalStateException("임시 예약된 좌석만 예약 확정할 수 있습니다.");
        }
        this.status = SeatStatus.RESERVED;
        this.temporaryReservedAt = null;
    }

    public void cancelReservation() {
        this.status = SeatStatus.AVAILABLE;
        this.temporaryReservedAt = null;
    }

    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    public boolean isTemporaryReservationExpired() {
        if (this.status != SeatStatus.TEMPORARILY_RESERVED || this.temporaryReservedAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.temporaryReservedAt.plusMinutes(15));
    }

    public String getSeatDisplay() {
        return seatRow + "-" + seatNumber;
    }
}