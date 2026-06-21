package org.example.virtuber.trade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TradeRequest {

    @NotNull(message = "종목 ID는 필수로 입력해야 합니다.")
    private Long stockId;

    @NotNull(message = "수량은 필수로 입력해야 합니다.")
    @Positive(message = "수량은 1주 이상이어야 합니다.")
    private Long quantity;
}
