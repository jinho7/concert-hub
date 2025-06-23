package com.concerthub.domain.seat.controller;

import com.concerthub.domain.seat.dto.request.SeatCreateRequest;
import com.concerthub.domain.seat.dto.response.SeatResponse;
import com.concerthub.domain.seat.dto.response.SeatSummaryResponse;
import com.concerthub.domain.seat.entity.Seat;
import com.concerthub.domain.seat.service.SeatService;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import com.concerthub.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events/{eventId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<SeatResponse>> createSeats(
            @PathVariable Long eventId,
            @Valid @RequestBody SeatCreateRequest request) {

        List<Seat> seats = seatService.createSeats(
                eventId,
                request.getTotalRows(),
                request.getSeatsPerRow(),
                request.getBasePrice()
        );

        List<SeatResponse> responses = seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses,
                String.format("총 %d개의 좌석이 생성되었습니다.", seats.size()));
    }

    @GetMapping
    public ApiResponse<List<SeatResponse>> getEventSeats(@PathVariable Long eventId) {
        List<Seat> seats = seatService.getEventSeats(eventId);
        List<SeatResponse> responses = seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @GetMapping("/available")
    public ApiResponse<List<SeatResponse>> getAvailableSeats(@PathVariable Long eventId) {
        List<Seat> seats = seatService.getAvailableSeats(eventId);
        List<SeatResponse> responses = seats.stream()
                .map(SeatResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @GetMapping("/summary")
    public ApiResponse<SeatSummaryResponse> getSeatSummary(@PathVariable Long eventId) {
        List<Seat> seats = seatService.getEventSeats(eventId);

        Map<String, Integer> seatCounts = seats.stream()
                .collect(Collectors.groupingBy(
                        seat -> seat.getStatus().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

        Integer minPrice = seats.stream().mapToInt(Seat::getPrice).min().orElse(0);
        Integer maxPrice = seats.stream().mapToInt(Seat::getPrice).max().orElse(0);

        SeatSummaryResponse summary = SeatSummaryResponse.of(eventId, seatCounts, minPrice, maxPrice);
        return ApiResponse.success(summary);
    }

    @GetMapping("/{seatId}")
    public ApiResponse<SeatResponse> getSeat(@PathVariable Long eventId, @PathVariable Long seatId) {
        Seat seat = seatService.getSeat(seatId);

        // 이벤트 ID 검증 (해당 이벤트의 좌석인지 확인)
        if (!seat.getEvent().getId().equals(eventId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "해당 이벤트의 좌석이 아닙니다.");
        }

        return ApiResponse.success(SeatResponse.from(seat));
    }

    @PostMapping("/{seatId}/temporary-reserve")
    public ApiResponse<SeatResponse> temporaryReserveSeat(
            @PathVariable Long eventId,
            @PathVariable Long seatId) {

        Seat seat = seatService.temporaryReserveSeat(seatId);
        return ApiResponse.success(SeatResponse.from(seat), "좌석이 임시 예약되었습니다.");
    }

    @PostMapping("/{seatId}/confirm")
    public ApiResponse<SeatResponse> confirmReservation(
            @PathVariable Long eventId,
            @PathVariable Long seatId) {

        Seat seat = seatService.confirmReservation(seatId);
        return ApiResponse.success(SeatResponse.from(seat), "좌석 예약이 확정되었습니다.");
    }

    @DeleteMapping("/{seatId}/cancel")
    public ApiResponse<SeatResponse> cancelReservation(
            @PathVariable Long eventId,
            @PathVariable Long seatId) {

        Seat seat = seatService.cancelReservation(seatId);
        return ApiResponse.success(SeatResponse.from(seat), "좌석 예약이 취소되었습니다.");
    }
}