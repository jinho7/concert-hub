package com.concerthub.global.jwt.dto;

public record JwtDto(
        String accessToken,
        String refreshToken
) {
}
