package com.concerthub.global.jwt.userdetails;

import com.concerthub.domain.user.entity.User;
import com.concerthub.domain.user.repository.UserRepository;
import com.concerthub.global.jwt.exception.JwtErrorCode;
import com.concerthub.global.jwt.exception.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("사용자 인증 시도: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음: {}", email);
                    return new JwtException(JwtErrorCode.USER_NOT_FOUND);
                });

        log.info("사용자 인증 성공: {}", email);
        return new CustomUserDetails(user);
    }
}
