package com.concerthub.global.jwt.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum JwtErrorCode {
    
    // 토큰 관련 에러
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_001", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "JWT_002", "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT_003", "토큰이 없습니다."),
    
    // 인증 관련 에러
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT_004", "사용자를 찾을 수 없습니다."),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "JWT_005", "잘못된 인증 정보입니다."),
    
    // 권한 관련 에러
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "JWT_006", "접근 권한이 없습니다."),
    INSUFFICIENT_PRIVILEGES(HttpStatus.FORBIDDEN, "JWT_007", "권한이 부족합니다."),
    
    // 리프레시 토큰 관련 에러
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT_008", "리프레시 토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_009", "리프레시 토큰이 만료되었습니다."),
    
    // 내부 에러
    INTERNAL_SECURITY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JWT_010", "보안 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    JwtErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
