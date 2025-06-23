package com.concerthub.domain.event.controller;

import com.concerthub.domain.event.dto.request.EventCreateRequest;
import com.concerthub.domain.event.dto.request.EventUpdateRequest;
import com.concerthub.domain.event.dto.response.EventResponse;
import com.concerthub.domain.event.entity.Event;
import com.concerthub.domain.event.service.EventService;
import com.concerthub.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        Event event = eventService.createEvent(
                request.getTitle(),
                request.getDescription(),
                request.getVenue(),
                request.getEventDateTime(),
                request.getTotalSeats(),
                request.getPrice()
        );

        return ApiResponse.success(EventResponse.from(event), "이벤트가 성공적으로 생성되었습니다.");
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        List<EventResponse> responses = events.stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEvent(@PathVariable Long id) {
        Event event = eventService.getEvent(id);
        return ApiResponse.success(EventResponse.from(event));
    }

    @GetMapping("/available")
    public ApiResponse<List<EventResponse>> getAvailableEvents() {
        List<Event> events = eventService.getAvailableEvents();
        List<EventResponse> responses = events.stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<EventResponse>> getUpcomingEvents() {
        List<Event> events = eventService.getUpcomingEvents();
        List<EventResponse> responses = events.stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest request) {
        Event event = eventService.updateEvent(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getVenue(),
                request.getEventDateTime(),
                request.getTotalSeats(),
                request.getPrice()
        );

        return ApiResponse.success(EventResponse.from(event), "이벤트가 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ApiResponse.success(null, "이벤트가 성공적으로 삭제되었습니다.");
    }
}