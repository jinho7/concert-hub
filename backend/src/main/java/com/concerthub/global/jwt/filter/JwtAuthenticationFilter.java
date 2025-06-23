package com.concerthub.global.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.concerthub.domain.auth.dto.request.LoginRequest;
import com.concerthub.global.jwt.dto.JwtDto;
import com.concerthub.global.jwt.exception.JwtErrorCode;
import com.concerthub.global.jwt.exception.JwtException;
import com.concerthub.global.jwt.userdetails.CustomUserDetails;
import com.concerthub.global.jwt.util.JwtUtil;
import com.concerthub.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        log.info("로그인 시도 중");

        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());

            return authenticationManager.authenticate(authToken);
            
        } catch (IOException e) {
            log.error("로그인 요청 파싱 실패", e);
            throw new JwtException(JwtErrorCode.INTERNAL_SECURITY_ERROR);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
                                          FilterChain chain, Authentication authResult) throws IOException {
        
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        log.info("로그인 성공: {}", userDetails.getUsername());

        // 성공 응답만 반환 (실제 JWT 토큰은 AuthController에서 처리)
        ApiResponse<String> successResponse = ApiResponse.onSuccess("로그인 성공");
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(successResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationException failed) throws IOException {
        
        log.info("로그인 실패: {}", failed.getMessage());

        JwtErrorCode errorCode;
        if (failed instanceof UsernameNotFoundException) {
            errorCode = JwtErrorCode.USER_NOT_FOUND;
        } else if (failed instanceof BadCredentialsException) {
            errorCode = JwtErrorCode.BAD_CREDENTIALS;
        } else {
            errorCode = JwtErrorCode.INTERNAL_SECURITY_ERROR;
        }

        ApiResponse<Void> errorResponse = ApiResponse.onFailure(
            errorCode.getCode(), 
            errorCode.getMessage(), 
            null
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(errorCode.getHttpStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
