package org.example.virtuber.account.dto;

public record HoldingResponse(
        Long stockId,
        String stockCode,
        String stockName,
        Long quantity,
        Long averagePrice,
        Long currentPrice,
        Long totalPurchaseAmount,
        Long evaluationAmount,
        Long profitAmount,
        Double profitRate
) {
}
