package com.concerthub.global.jwt.exception;

public class JwtException extends RuntimeException {
    
    private final String errorCode;
    
    public JwtException(JwtErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
    }
    
    public JwtException(JwtErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode.getCode();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
