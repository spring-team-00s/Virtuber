package org.example.virtuber.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.virtuber.auth.dto.*;
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
    public ApiResponse<SigninResponse> signin(
            @Valid @RequestBody SigninRequest request
    ) {
        return ApiResponse.ok(
                authService.signin(request),
                "로그인에 성공했습니다."
        );
    }

    @PostMapping("/reissue")
    public ApiResponse<ReissueResponse> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        return ApiResponse.ok(
                authService.reissue(request),
                "Access Token 재발급에 성공했습니다."
        );
    }

    @PostMapping("/signout")
    public ApiResponse<Void> signout(
            @Valid @RequestBody SignoutRequest request
    ) {
        authService.signout(request);
        return ApiResponse.ok(
                null,
                "로그아웃에 성공했습니다."
        );
    }
}
