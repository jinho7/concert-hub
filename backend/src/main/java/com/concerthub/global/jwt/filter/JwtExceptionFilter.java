package com.concerthub.global.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.concerthub.global.jwt.exception.JwtException;
import com.concerthub.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("JWT 예외 발생: {}", e.getMessage());
            setErrorResponse(response, e);
        } catch (Exception e) {
            log.error("필터에서 예상치 못한 예외 발생", e);
            setErrorResponse(response, new JwtException(
                com.concerthub.global.jwt.exception.JwtErrorCode.INTERNAL_SECURITY_ERROR));
        }
    }

    private void setErrorResponse(HttpServletResponse response, JwtException e) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Void> errorResponse = ApiResponse.onFailure(
            e.getErrorCode(),
            e.getMessage(),
            null
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
