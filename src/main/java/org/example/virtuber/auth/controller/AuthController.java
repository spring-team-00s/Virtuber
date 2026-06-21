package org.example.virtuber.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.virtuber.auth.dto.AuthResponse;
import org.example.virtuber.auth.dto.RegisterRequest;
import org.example.virtuber.auth.dto.SigninRequest;
import org.example.virtuber.auth.dto.SigninResponse;
import org.example.virtuber.auth.service.AuthService;
import org.example.virtuber.common.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ApiResponse.ok(
                authService.register(request),
                "회원가입에 성공했습니다."
        );
    }

    @PostMapping("/signin")
    public ApiResponse<SigninResponse> siginin(
            @Valid @RequestBody SigninRequest request
    ) {
        return ApiResponse.ok(
                authService.signin(request),
                "로그인에 성공했습니다."
        );
    }
}
