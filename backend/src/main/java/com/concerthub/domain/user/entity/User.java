package com.concerthub.domain.user.entity;

import com.concerthub.domain.user.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String name, String email, String phoneNumber, String password, UserRole role) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role != null ? role : UserRole.USER;
    }

    public static User createUser(String name, String email, String phoneNumber, String rawPassword, PasswordEncoder passwordEncoder) {
        return User.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.USER)
                .build();
    }

    public static User createAdmin(String name, String email, String phoneNumber, String rawPassword, PasswordEncoder passwordEncoder) {
        return User.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.ADMIN)
                .build();
    }
}