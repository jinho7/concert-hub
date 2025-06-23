package com.concerthub.domain.auth.service;

import com.concerthub.domain.auth.dto.request.LoginRequest;
import com.concerthub.domain.user.entity.User;
import com.concerthub.domain.user.repository.UserRepository;
import com.concerthub.global.jwt.dto.JwtDto;
import com.concerthub.global.jwt.exception.JwtErrorCode;
import com.concerthub.global.jwt.exception.JwtException;
import com.concerthub.global.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public JwtDto login(LoginRequest loginRequest) {
        // 사용자 조회
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new JwtException(JwtErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new JwtException(JwtErrorCode.BAD_CREDENTIALS);
        }

        // JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        log.info("로그인 성공: 사용자ID={}, 이메일={}", user.getId(), user.getEmail());

        return new JwtDto(accessToken, refreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new JwtException(JwtErrorCode.USER_NOT_FOUND));

        // 리프레시 토큰 삭제
        jwtUtil.deleteRefreshToken(user);

        log.info("로그아웃 완료: 사용자ID={}", userId);
    }

    @Transactional
    public JwtDto refresh(String refreshToken) {
        return jwtUtil.reissueTokens(refreshToken);
    }
}
