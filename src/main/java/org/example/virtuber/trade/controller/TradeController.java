package org.example.virtuber.trade.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.virtuber.common.response.ApiResponse;
import org.example.virtuber.trade.dto.TradeHistoryResponse;
import org.example.virtuber.trade.dto.TradeRequest;
import org.example.virtuber.trade.dto.TradeResponse;
import org.example.virtuber.trade.service.TradeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trades")
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/buy")
    public ApiResponse<TradeResponse> buy(
            Authentication authentication,
            @Valid @RequestBody TradeRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                tradeService.buy(userId, request),
                "매수 완료"
        );
    }

    @PostMapping("/sell")
    public ApiResponse<TradeResponse> sell(
            Authentication authentication,
            @Valid @RequestBody TradeRequest request
    ) {
        long userId = (Long) authentication.getPrincipal();
        return ApiResponse.ok(
                tradeService.sell(userId, request),
                "매도 완료"
        );
    }

    @GetMapping
    public ApiResponse<List<TradeHistoryResponse>> getTrades(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return ApiResponse.ok(
                tradeService.getTrades(userId),
                "거래 내역 조회 성공."
        );
    }
}
