package com.concerthub.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입 값입니다."),

    // 이벤트 관련 에러
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "존재하지 않는 이벤트입니다."),
    EVENT_ALREADY_SOLD_OUT(HttpStatus.BAD_REQUEST, "E002", "이미 매진된 이벤트입니다."),
    EVENT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "E003", "예약할 수 없는 이벤트입니다."),
    INVALID_SEAT_COUNT(HttpStatus.BAD_REQUEST, "E004", "좌석 수가 올바르지 않습니다."),

    // 좌석 관련 에러
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "존재하지 않는 좌석입니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "S002", "이미 예약된 좌석입니다."),
    SEAT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "S003", "예약할 수 없는 좌석입니다."),
    SEAT_TEMPORARILY_RESERVED(HttpStatus.BAD_REQUEST, "S004", "임시 예약된 좌석입니다."),
    SEAT_RESERVATION_EXPIRED(HttpStatus.BAD_REQUEST, "S005", "좌석 예약 시간이 만료되었습니다."),
    INVALID_SEAT_OPERATION(HttpStatus.BAD_REQUEST, "S006", "잘못된 좌석 작업입니다."),

    // 예약 관련 에러
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "존재하지 않는 예약입니다."),
    RESERVATION_EXPIRED(HttpStatus.BAD_REQUEST, "R002", "만료된 예약입니다."),
    RESERVATION_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "R003", "이미 확정된 예약입니다."),
    RESERVATION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "R004", "이미 취소된 예약입니다."),
    RESERVATION_NOT_PENDING(HttpStatus.BAD_REQUEST, "R005", "대기 중인 예약이 아닙니다."),

    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    USER_EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 이메일입니다."),

    // 결제 관련 에러
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "P001", "결제 처리에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "존재하지 않는 결제입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}