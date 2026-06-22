package org.example.virtuber.trade.service;

import lombok.RequiredArgsConstructor;
import org.example.virtuber.account.repository.AccountRepository;
import org.example.virtuber.account.entity.Account;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.stock.entity.Stock;
import org.example.virtuber.stock.repository.StockRepository;
import org.example.virtuber.trade.dto.TradeHistoryResponse;
import org.example.virtuber.trade.dto.TradeRequest;
import org.example.virtuber.trade.dto.TradeResponse;
import org.example.virtuber.trade.entity.Holding;
import org.example.virtuber.trade.entity.Trade;
import org.example.virtuber.trade.repository.HoldingRepository;
import org.example.virtuber.trade.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;

    @Transactional
    public TradeResponse buy(Long userId, TradeRequest request) {
        // 계좌 및 종목 조회
        Account account = getAccountForUpdate(userId);
        Stock stock = getStock(request.getStockId());
        // 총 매수금액 계산
        Long quantity = request.getQuantity();
        Long price = stock.getCurrentPrice();
        Long totalAmount = price * quantity;
        // 현금 차감
        account.decreaseCash(totalAmount);

        Optional<Holding> optionalHolding =
                holdingRepository.findByAccount_IdAndStock_Id(account.getId(), stock.getId());

        Holding holding;
        // 보유 종목 조회
        if(optionalHolding.isPresent()) {
            holding = optionalHolding.get();
            holding.buy(quantity, price);
        } else {
            holding = new Holding(account, stock, quantity, price);
            holdingRepository.save(holding);
        }

        Trade trade = tradeRepository.save(
                Trade.buy(account, stock, quantity, price)
        );

        return TradeResponse.from(trade, account);
    }

    @Transactional
    public TradeResponse sell(Long userId, TradeRequest request) {
        Account account = getAccountForUpdate(userId);
        Stock stock = getStock(request.getStockId());

        Long quantity = request.getQuantity();
        Long price = stock.getCurrentPrice();
        long totalAmount = price * quantity;

        Holding holding = holdingRepository
                .findByAccount_IdAndStock_Id(account.getId(), stock.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_HOLDING));

        Long averagePriceBeforeSell = holding.getAveragePrice();
        Long sellProfitAmount = (price - averagePriceBeforeSell) * quantity;
        Double sellProfitRate = calculateProfitRate(price, averagePriceBeforeSell);

        holding.sell(quantity);
        account.increaseCash(totalAmount);

        Trade trade = tradeRepository.save(
                Trade.sell(account, stock, quantity, price, averagePriceBeforeSell, sellProfitAmount, sellProfitRate)
        );

        if(holding.isEmpty()) {
            holdingRepository.delete(holding);
        }

        return TradeResponse.from(trade, account);
    }

    private Account getAccountForUpdate(Long userId) {
        return accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private Stock getStock(Long stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));
    }

    private Double calculateProfitRate(Long sellPrice, Long averagePriceBeforeSell) {
        if(averagePriceBeforeSell == null || averagePriceBeforeSell == 0) {
            return 0.0;
        }
        double profitRate = (sellPrice.doubleValue() - averagePriceBeforeSell.doubleValue())
            / averagePriceBeforeSell.doubleValue() * 100;

        return Math.round(profitRate * 100.0) / 100.0;
    }

    // 거래 내역 조회
    @Transactional(readOnly = true)
    public List<TradeHistoryResponse> getTrades(Long userId) {
        Account account = accountRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        return tradeRepository.findTradeHistoriesByAccountId(account.getId())
                .stream()
                .map(TradeHistoryResponse::from)
                .toList();
    }
}
