package com.concerthub.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ErrorResponse {

    private String message;
    private String code;
    private int status;
    private LocalDateTime timestamp;
    private List<FieldError> errors;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .message(errorCode.getMessage())
                .code(errorCode.getCode())
                .status(errorCode.getHttpStatus().value())
                .timestamp(LocalDateTime.now())
                .errors(new ArrayList<>())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .message(message)
                .code(errorCode.getCode())
                .status(errorCode.getHttpStatus().value())
                .timestamp(LocalDateTime.now())
                .errors(new ArrayList<>())
                .build();
    }

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .message(message)
                .code(code)
                .status(401) // JWT 예외는 기본적으로 401
                .timestamp(LocalDateTime.now())
                .errors(new ArrayList<>())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .message(errorCode.getMessage())
                .code(errorCode.getCode())
                .status(errorCode.getHttpStatus().value())
                .timestamp(LocalDateTime.now())
                .errors(FieldError.of(bindingResult))
                .build();
    }

    @Getter
    @Builder
    public static class FieldError {

        private String field;
        private String value;
        private String reason;

        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> FieldError.builder()
                            .field(error.getField())
                            .value(error.getRejectedValue() == null ? "" : error.getRejectedValue().toString())
                            .reason(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}