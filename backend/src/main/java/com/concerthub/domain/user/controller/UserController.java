package com.concerthub.domain.user.controller;

import com.concerthub.domain.user.dto.request.UserCreateRequest;
import com.concerthub.domain.user.dto.response.UserResponse;
import com.concerthub.domain.user.entity.User;
import com.concerthub.domain.user.service.UserService;
import com.concerthub.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        return ApiResponse.success(UserResponse.from(user), "사용자가 성공적으로 생성되었습니다.");
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ApiResponse.success(UserResponse.from(user));
    }

    @GetMapping("/email/{email}")
    public ApiResponse<UserResponse> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ApiResponse.success(UserResponse.from(user));
    }
}