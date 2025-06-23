package com.concerthub.domain.auth.controller;

import com.concerthub.domain.auth.dto.request.LoginRequest;
import com.concerthub.domain.auth.service.AuthService;
import com.concerthub.global.jwt.dto.JwtDto;
import com.concerthub.global.jwt.userdetails.CustomUserDetails;
import com.concerthub.global.jwt.util.JwtUtil;
import com.concerthub.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtDto>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtDto jwtDto = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.onSuccess(jwtDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {
        
        String accessToken = jwtUtil.resolveAccessToken(request);
        authService.logout(userDetails.getId(), accessToken);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtDto>> refresh(@RequestBody RefreshTokenRequest request) {
        JwtDto jwtDto = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.onSuccess(jwtDto));
    }

    public record RefreshTokenRequest(String refreshToken) {}
}
