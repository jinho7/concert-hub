package com.concerthub.domain.event.repository;

import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.event.entity.status.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // 상태별 이벤트 조회
    List<Event> findByStatus(EventStatus status);

    // 특정 날짜 이후 이벤트 조회 (예정된 이벤트)
    List<Event> findByEventDateTimeAfterOrderByEventDateTimeAsc(LocalDateTime dateTime);

    // 예약 가능한 이벤트 조회 (잔여석 있는 OPEN 상태)
    @Query("SELECT e FROM Event e WHERE e.status = :status AND e.availableSeats > 0")
    List<Event> findAvailableEvents(@Param("status") EventStatus status);

    // 장소별 이벤트 조회
    List<Event> findByVenueContainingIgnoreCase(String venue);
}