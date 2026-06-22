package org.example.virtuber.trade.service;

import org.example.virtuber.account.entity.Account;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.stock.entity.Stock;
import org.example.virtuber.support.IntegrationTestSupport;
import org.example.virtuber.trade.dto.TradeResponse;
import org.example.virtuber.trade.entity.Holding;
import org.example.virtuber.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeServiceTest extends IntegrationTestSupport {

    @Autowired
    TradeService tradeService;

    @Test
    @DisplayName("매수 성공 시 현금 잔고를 차감하고 보유 주식과 거래 내역을 저장한다")
    void buySucceeds() {
        User user = saveUser("zero");
        Account account = saveAccount(user, 10_000_000L);
        Stock stock = saveStock("005930", "삼성전자", 78_000L);

        TradeResponse response = tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

        Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();
        Holding holding = holdingRepository.findByAccount_IdAndStock_Id(account.getId(), stock.getId())
                .orElseThrow();

        assertThat(response.getTradeType()).isEqualTo("BUY");
        assertThat(response.getTotalAmount()).isEqualTo(780_000L);
        assertThat(response.getCashBalance()).isEqualTo(9_220_000L);
        assertThat(refreshedAccount.getCashBalance()).isEqualTo(9_220_000L);
        assertThat(holding.getQuantity()).isEqualTo(10L);
        assertThat(holding.getAveragePrice()).isEqualTo(78_000L);
        assertThat(tradeRepository.findAllByAccount_IdOrderByTradedTimeDesc(account.getId())).hasSize(1);
    }

    @Test
    @DisplayName("잔고 부족으로 매수 실패 시 계좌, 보유 주식, 거래 내역을 변경하지 않는다")
    void buyFailsWhenCashIsInsufficient() {
        User user = saveUser("zero");
        Account account = saveAccount(user, 100_000L);
        Stock stock = saveStock("005930", "삼성전자", 78_000L);

        assertThatThrownBy(() -> tradeService.buy(user.getId(), tradeRequest(stock.getId(), 2L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_CASH);

        Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(refreshedAccount.getCashBalance()).isEqualTo(100_000L);
        assertThat(holdingRepository.findAllByAccount_Id(account.getId())).isEmpty();
        assertThat(tradeRepository.findAllByAccount_IdOrderByTradedTimeDesc(account.getId())).isEmpty();
    }

    @Test
    @DisplayName("추가 매수 시 평균 매입가를 재계산한다")
    void additionalBuyRecalculatesAveragePrice() {
        User user = saveUser("zero");
        Account account = saveAccount(user, 10_000_000L);
        Stock samsung = saveStock("005930", "삼성전자", 78_000L);

        tradeService.buy(user.getId(), tradeRequest(samsung.getId(), 10L));
        tradeService.buy(user.getId(), tradeRequest(samsung.getId(), 5L));

        Holding holding = holdingRepository.findByAccount_IdAndStock_Id(account.getId(), samsung.getId())
                .orElseThrow();
        assertThat(holding.getQuantity()).isEqualTo(15L);
        assertThat(holding.getAveragePrice()).isEqualTo(78_000L);
    }

    @Test
    @DisplayName("매도 성공 시 보유 수량을 줄이고 현금 잔고를 증가시킨다")
    void sellSucceeds() {
        User user = saveUser("zero");
        Account account = saveAccount(user, 10_000_000L);
        Stock stock = saveStock("005930", "삼성전자", 78_000L);
        tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

        TradeResponse response = tradeService.sell(user.getId(), tradeRequest(stock.getId(), 4L));

        Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();
        Holding holding = holdingRepository.findByAccount_IdAndStock_Id(account.getId(), stock.getId())
                .orElseThrow();

        assertThat(response.getTradeType()).isEqualTo("SELL");
        assertThat(response.getTotalAmount()).isEqualTo(312_000L);
        assertThat(response.getCashBalance()).isEqualTo(9_532_000L);
        assertThat(refreshedAccount.getCashBalance()).isEqualTo(9_532_000L);
        assertThat(holding.getQuantity()).isEqualTo(6L);
        assertThat(tradeRepository.findAllByAccount_IdOrderByTradedTimeDesc(account.getId())).hasSize(2);
    }

    @Test
    @DisplayName("보유 수량보다 많이 매도하면 실패한다")
    void sellFailsWhenQuantityExceedsHolding() {
        User user = saveUser("zero");
        Account account = saveAccount(user, 10_000_000L);
        Stock stock = saveStock("005930", "삼성전자", 78_000L);
        tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

        assertThatThrownBy(() -> tradeService.sell(user.getId(), tradeRequest(stock.getId(), 11L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_HOLDING);

        Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();
        Holding holding = holdingRepository.findByAccount_IdAndStock_Id(account.getId(), stock.getId())
                .orElseThrow();
        assertThat(refreshedAccount.getCashBalance()).isEqualTo(9_220_000L);
        assertThat(holding.getQuantity()).isEqualTo(10L);
        assertThat(tradeRepository.findAllByAccount_IdOrderByTradedTimeDesc(account.getId())).hasSize(1);
    }

    @Test
    @DisplayName("매도 후 보유 수량이 0이면 Holding을 삭제한다")
    void sellDeletesHoldingWhenQuantityBecomesZero() {
        User user = saveUser("zero");
        Account account = saveAccount(user, 10_000_000L);
        Stock stock = saveStock("005930", "삼성전자", 78_000L);
        tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

        tradeService.sell(user.getId(), tradeRequest(stock.getId(), 10L));

        Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(refreshedAccount.getCashBalance()).isEqualTo(10_000_000L);
        assertThat(holdingRepository.findByAccount_IdAndStock_Id(account.getId(), stock.getId())).isEmpty();
    }
}
