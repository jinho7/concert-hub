package com.concerthub.domain.event.service;

import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.event.entity.status.EventStatus;
import com.concerthub.domain.event.repository.EventRepository;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public Event createEvent(String title, String description, String venue,
                             LocalDateTime eventDateTime, Integer totalSeats, Integer price) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .venue(venue)
                .eventDateTime(eventDateTime)
                .totalSeats(totalSeats)
                .price(price)
                .build();

        return eventRepository.save(event);
    }

    public Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getAvailableEvents() {
        return eventRepository.findAvailableEvents(EventStatus.OPEN);
    }

    public List<Event> getUpcomingEvents() {
        return eventRepository.findByEventDateTimeAfterOrderByEventDateTimeAsc(LocalDateTime.now());
    }

    @Transactional
    public Event updateEvent(Long eventId, String title, String description, String venue,
                             LocalDateTime eventDateTime, Integer totalSeats, Integer price) {
        Event event = getEvent(eventId);

        // 기존 예약이 있는 상태에서 좌석 수를 줄이는 경우 검증
        if (totalSeats != null && totalSeats < (event.getTotalSeats() - event.getAvailableSeats())) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_COUNT,
                    "예약된 좌석보다 적은 수로 변경할 수 없습니다.");
        }

        event.updateEvent(title, description, venue, eventDateTime, totalSeats, price);
        return event;
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = getEvent(eventId);

        // 예약이 있는 이벤트는 삭제 불가
        if (event.getAvailableSeats() < event.getTotalSeats()) {
            throw new BusinessException(ErrorCode.EVENT_NOT_AVAILABLE,
                    "예약이 있는 이벤트는 삭제할 수 없습니다.");
        }

        eventRepository.delete(event);
    }
}