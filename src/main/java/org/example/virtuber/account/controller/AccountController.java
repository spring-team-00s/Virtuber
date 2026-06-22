package org.example.virtuber.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.virtuber.account.dto.AccountInitRequest;
import org.example.virtuber.account.dto.AccountInitResponse;
import org.example.virtuber.account.dto.AccountMeResponse;
import org.example.virtuber.account.service.AccountService;
import org.example.virtuber.common.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ApiResponse<AccountMeResponse> getMyAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                accountService.getMyAccount(userId),
                "계좌 조회에 성공했습니다."
        );
    }

    @PutMapping("/me/init")
    public ApiResponse<AccountInitResponse> initMyAccount(
        Authentication authentication,
        @Valid @RequestBody AccountInitRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
            accountService.initMyAccount(userId, request),
            "계좌 초기화에 성공했습니다."
        );
    }
}
