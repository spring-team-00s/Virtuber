package org.example.virtuber.trade.service;

import org.example.virtuber.account.entity.Account;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.stock.entity.Stock;
import org.example.virtuber.support.IntegrationTestSupport;
import org.example.virtuber.trade.entity.Holding;
import org.example.virtuber.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TradeConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    TradeService tradeService;

    @Test
    @DisplayName("동시 매수 요청 시 잔고 초과를 방지한다")
    void concurrentBuyPreventsNegativeCashBalance() throws Exception {
        User user = saveUser("zero");
        Account account = saveAccount(user, 100_000L);
        Stock stock = saveStock("005930", "삼성전자", 60_000L);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger insufficientCashCount = new AtomicInteger();

        runConcurrently(2, () -> {
            try {
                tradeService.buy(user.getId(), tradeRequest(stock.getId(), 1L));
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.INSUFFICIENT_CASH) {
                    insufficientCashCount.incrementAndGet();
                    return;
                }
                throw e;
            }
        });

        Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();
        Holding holding = holdingRepository.findByAccount_IdAndStock_Id(account.getId(), stock.getId())
                .orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(insufficientCashCount.get()).isEqualTo(1);
        assertThat(refreshedAccount.getCashBalance()).isEqualTo(40_000L);
        assertThat(holding.getQuantity()).isEqualTo(1L);
        assertThat(tradeRepository.findAllByAccount_IdOrderByTradedTimeDesc(account.getId())).hasSize(1);
    }

    @Test
    @DisplayName("동시 매도 요청 시 보유 수량 초과를 방지한다")
    void concurrentSellPreventsNegativeHoldingQuantity() throws Exception {
        User user = saveUser("zero");
        Account account = saveAccount(user, 10_000_000L);
        Stock stock = saveStock("005930", "삼성전자", 60_000L);
        tradeService.buy(user.getId(), tradeRequest(stock.getId(), 1L));

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger insufficientHoldingCount = new AtomicInteger();

        runConcurrently(2, () -> {
            try {
                tradeService.sell(user.getId(), tradeRequest(stock.getId(), 1L));
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                if (e.getErrorCode() == ErrorCode.INSUFFICIENT_HOLDING) {
                    insufficientHoldingCount.incrementAndGet();
                    return;
                }
                throw e;
            }
        });

        Account refreshedAccount = accountRepository.findById(account.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(insufficientHoldingCount.get()).isEqualTo(1);
        assertThat(refreshedAccount.getCashBalance()).isEqualTo(10_000_000L);
        assertThat(holdingRepository.findByAccount_IdAndStock_Id(account.getId(), stock.getId())).isEmpty();
        assertThat(tradeRepository.findAllByAccount_IdOrderByTradedTimeDesc(account.getId())).hasSize(2);
    }

    private void runConcurrently(int threadCount, Runnable task) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                task.run();
                return null;
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();
    }
}
