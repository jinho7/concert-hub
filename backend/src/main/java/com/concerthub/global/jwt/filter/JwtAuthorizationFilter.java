package com.concerthub.global.jwt.filter;

import com.concerthub.domain.user.entity.enums.UserRole;
import com.concerthub.global.jwt.exception.JwtErrorCode;
import com.concerthub.global.jwt.exception.JwtException;
import com.concerthub.global.jwt.userdetails.CustomUserDetails;
import com.concerthub.global.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String accessToken = jwtUtil.resolveAccessToken(request);
        
        if (accessToken != null) {
            try {
                // 토큰 검증
                jwtUtil.validateToken(accessToken);
                
                // 토큰에서 사용자 정보 추출
                Long userId = jwtUtil.getUserId(accessToken);
                String email = jwtUtil.getEmail(accessToken);
                String roleString = jwtUtil.getRole(accessToken);
                UserRole role = UserRole.valueOf(roleString.replace("ROLE_", ""));
                
                // CustomUserDetails 생성
                CustomUserDetails userDetails = new CustomUserDetails(userId, email, null, role, null);
                
                // Authentication 객체 생성 및 SecurityContext에 설정
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 인증 성공: userId={}, email={}", userId, email);
                
            } catch (JwtException e) {
                log.warn("JWT 토큰 검증 실패: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                // 예외를 던져서 JwtExceptionFilter에서 처리하도록 함
                throw e;
            } catch (Exception e) {
                log.error("JWT 처리 중 예상치 못한 오류", e);
                SecurityContextHolder.clearContext();
                throw new JwtException(JwtErrorCode.INTERNAL_SECURITY_ERROR, e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
