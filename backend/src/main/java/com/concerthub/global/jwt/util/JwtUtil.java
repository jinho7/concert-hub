package com.concerthub.global.jwt.util;

import com.concerthub.domain.auth.entity.BlacklistedToken;
import com.concerthub.domain.auth.entity.RefreshToken;
import com.concerthub.domain.auth.repository.BlacklistedTokenRepository;
import com.concerthub.domain.auth.repository.RefreshTokenRepository;
import com.concerthub.domain.user.entity.User;
import com.concerthub.global.jwt.dto.JwtDto;
import com.concerthub.global.jwt.exception.JwtErrorCode;
import com.concerthub.global.jwt.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") Long accessExpiration,
            @Value("${jwt.refresh-token-expiration}") Long refreshExpiration,
            RefreshTokenRepository refreshTokenRepository,
            BlacklistedTokenRepository blacklistedTokenRepository) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessTokenExpiration = accessExpiration;
        this.refreshTokenExpiration = refreshExpiration;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    public Long getUserId(String token) {
        validateToken(token);
        String userIdString = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(userIdString);
    }

    public String getEmail(String token) {
        validateToken(token);
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    public String getRole(String token) {
        validateToken(token);
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getValue())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    @Transactional
    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(refreshTokenExpiration);

        String refreshToken = Jwts.builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getValue())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();

        // 기존 리프레시 토큰이 있으면 삭제
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        // 새 리프레시 토큰 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.ofInstant(expiration, ZoneId.systemDefault()))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return refreshToken;
    }

    @Transactional
    public JwtDto reissueTokens(String refreshTokenValue) {
        // 리프레시 토큰 검증
        validateToken(refreshTokenValue);

        // DB에서 리프레시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new JwtException(JwtErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 토큰 만료 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new JwtException(JwtErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        // 새로운 토큰들 생성
        String newAccessToken = createAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        log.info("토큰 재발급 완료: 사용자ID={}", user.getId());

        return new JwtDto(newAccessToken, newRefreshToken);
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        return authorization.substring(7);
    }

    @Transactional
    public void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void validateToken(String token) {
        try {
            // 1. 블랙리스트 체크
            if (blacklistedTokenRepository.existsByToken(token)) {
                throw new JwtException(JwtErrorCode.TOKEN_EXPIRED);
            }

            // 2. JWT 유효성 검증
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new JwtException(JwtErrorCode.INVALID_TOKEN, e);
        } catch (ExpiredJwtException e) {
            throw new JwtException(JwtErrorCode.TOKEN_EXPIRED, e);
        }
    }

    @Transactional
    public void addToBlacklist(String token) {
        // 토큰에서 만료 시간 추출
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        Date expiration = claims.getExpiration();
        LocalDateTime expiresAt = expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .token(token)
                .expiresAt(expiresAt)
                .build();
        
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        blacklistedTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("만료된 리프레시 토큰 및 블랙리스트 토큰 정리 완료");
    }
}
