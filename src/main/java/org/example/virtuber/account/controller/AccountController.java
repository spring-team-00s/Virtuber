package org.example.virtuber.account.controller;

import lombok.RequiredArgsConstructor;
import org.example.virtuber.account.dto.AccountMeResponse;
import org.example.virtuber.account.service.AccountService;
import org.example.virtuber.common.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
