package org.example.virtuber.account.service;

import org.example.virtuber.account.dto.AccountInitRequest;
import org.example.virtuber.account.dto.AccountInitResponse;
import org.example.virtuber.account.dto.AccountMeResponse;
import org.example.virtuber.account.entity.Account;
import org.example.virtuber.stock.entity.Stock;
import org.example.virtuber.support.IntegrationTestSupport;
import org.example.virtuber.trade.service.TradeService;
import org.example.virtuber.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class AccountServiceTest extends IntegrationTestSupport {

  @Autowired
  AccountService accountService;

  @Autowired
  TradeService tradeService;

  @Test
  @DisplayName("계좌 조회 시 보유 주식을 함께 반환한다")
  void getMyAccountIncludesHoldings() {
    User user = saveUser("zero");
    saveAccount(user, 10_000_000L);
    Stock stock = saveStock("005930", "삼성전자", 78_000L);
    tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

    AccountMeResponse response = accountService.getMyAccount(user.getId());

    assertThat(response.cashBalance()).isEqualTo(9_220_000L);
    assertThat(response.totalPurchaseAmount()).isEqualTo(780_000L);
    assertThat(response.stockEvaluationAmount()).isEqualTo(780_000L);
    assertThat(response.totalAssetAmount()).isEqualTo(10_000_000L);
    assertThat(response.holdings()).hasSize(1);
    assertThat(response.holdings().get(0).stockId()).isEqualTo(stock.getId());
    assertThat(response.holdings().get(0).quantity()).isEqualTo(10L);
  }

  @Test
  @DisplayName("계좌 초기화 시 시드머니로 현금 잔고를 복구한다")
  void initMyAccountRestoresSeedMoney() {
    User user = saveUser("zero");
    Account account = saveAccount(user, 10_000_000L);
    Stock stock = saveStock("005930", "삼성전자", 78_000L);
    tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

    AccountInitResponse response =
        accountService.initMyAccount(user.getId(), new AccountInitRequest(RAW_PASSWORD));

    Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();
    assertThat(response.cashBalance()).isEqualTo(10_000_000L);
    assertThat(refreshedAccount.getCashBalance()).isEqualTo(10_000_000L);
  }

  @Test
  @DisplayName("계좌 초기화 시 보유 주식을 삭제한다")
  void initMyAccountDeletesHoldings() {
    User user = saveUser("zero");
    Account account = saveAccount(user, 10_000_000L);
    Stock stock = saveStock("005930", "삼성전자", 78_000L);
    tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

    AccountInitResponse response =
        accountService.initMyAccount(user.getId(), new AccountInitRequest(RAW_PASSWORD));

    assertThat(response.holdingsCount()).isZero();
    assertThat(holdingRepository.findAllByAccount_Id(account.getId())).isEmpty();
  }

  @Test
  @DisplayName("계좌 초기화 시 거래 내역을 삭제한다")
  void initMyAccountDeletesTrades() {
    User user = saveUser("zero");
    Account account = saveAccount(user, 10_000_000L);
    Stock stock = saveStock("005930", "삼성전자", 78_000L);
    tradeService.buy(user.getId(), tradeRequest(stock.getId(), 10L));

    accountService.initMyAccount(user.getId(), new AccountInitRequest(RAW_PASSWORD));

    assertThat(tradeRepository.findAllByAccount_IdOrderByTradedTimeDesc(account.getId())).isEmpty();
  }
}
