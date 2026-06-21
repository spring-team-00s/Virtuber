package org.example.virtuber.account.dto;

import java.util.List;

public record AccountMeResponse(
        Long cashBalance,
        Long totalPurchaseAmount,
        Long stockEvaluationAmount,
        Long totalAssetAmount,
        Long profitAmount,
        Double profitRate,
        List<HoldingResponse> holdings
) {
}
