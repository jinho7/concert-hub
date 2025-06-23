package com.concerthub.domain.event.entity;

import com.concerthub.domain.event.entity.status.EventStatus;
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
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 100)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime eventDateTime;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Event(String title, String description, String venue,
                 LocalDateTime eventDateTime, Integer totalSeats, Integer price) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.eventDateTime = eventDateTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats; // 초기에는 전체 좌석이 모두 available
        this.price = price;
        this.status = EventStatus.OPEN;
    }

    // 비즈니스 로직
    public void decreaseAvailableSeats() {
        if (this.availableSeats <= 0) {
            throw new IllegalStateException("예약 가능한 좌석이 없습니다.");
        }
        this.availableSeats--;

        if (this.availableSeats == 0) {
            this.status = EventStatus.SOLD_OUT;
        }
    }

    public void increaseAvailableSeats() {
        if (this.availableSeats >= this.totalSeats) {
            throw new IllegalStateException("가용 좌석이 전체 좌석보다 클 수 없습니다.");
        }
        this.availableSeats++;
        this.status = EventStatus.OPEN;
    }

    // 기존 코드에 추가
    public void updateEvent(String title, String description, String venue,
                            LocalDateTime eventDateTime, Integer totalSeats, Integer price) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (venue != null) this.venue = venue;
        if (eventDateTime != null) this.eventDateTime = eventDateTime;
        if (price != null) this.price = price;

        if (totalSeats != null) {
            // 좌석 수 변경 시 available 좌석도 조정
            int reservedSeats = this.totalSeats - this.availableSeats;
            this.totalSeats = totalSeats;
            this.availableSeats = totalSeats - reservedSeats;

            // 매진 상태 재계산
            if (this.availableSeats <= 0) {
                this.status = EventStatus.SOLD_OUT;
            } else {
                this.status = EventStatus.OPEN;
            }
        }
    }
}