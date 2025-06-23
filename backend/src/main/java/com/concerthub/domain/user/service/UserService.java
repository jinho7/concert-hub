package com.concerthub.domain.user.service;

import com.concerthub.domain.user.entity.User;
import com.concerthub.domain.user.repository.UserRepository;
import com.concerthub.global.exception.BusinessException;
import com.concerthub.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(String name, String email, String phoneNumber) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();

        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}