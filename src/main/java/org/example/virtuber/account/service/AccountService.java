package org.example.virtuber.account.service;

import lombok.RequiredArgsConstructor;
import org.example.virtuber.account.AccountRepository;
import org.example.virtuber.account.dto.AccountInitRequest;
import org.example.virtuber.account.dto.AccountInitResponse;
import org.example.virtuber.account.dto.AccountMeResponse;
import org.example.virtuber.account.dto.HoldingResponse;
import org.example.virtuber.account.entity.Account;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.trade.entity.Holding;
import org.example.virtuber.trade.repository.HoldingRepository;
import org.example.virtuber.trade.repository.TradeRepository;
import org.example.virtuber.user.entity.User;
import org.example.virtuber.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final HoldingRepository holdingRepository;
  private final TradeRepository tradeRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
    public AccountMeResponse getMyAccount(Long userId) {
      Account account = accountRepository.findByUser_Id(userId)
          .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

      List<Holding> holdings = holdingRepository.findAllByAccountIdWithStock(account.getId());

      Long totalPurchaseAmount = holdings.stream()
          .mapToLong(holding -> holding.getQuantity() * holding.getAveragePrice())
          .reduce(0L, Long::sum);

      Long stockEvaluationAmount = holdings.stream()
          .mapToLong(holding -> holding.getQuantity() * holding.getStock().getCurrentPrice())
          .reduce(0L, Long::sum);

      Long totalAssetAmount = account.getCashBalance() + stockEvaluationAmount;

      Long profitAmount = stockEvaluationAmount - totalPurchaseAmount;

      Double profitRate = totalPurchaseAmount == 0 ? 0.0 :
          profitAmount.doubleValue() / totalPurchaseAmount.doubleValue() * 100;

      List<HoldingResponse> holdingResponseList = holdings.stream()
          .map(holding -> {
            Long totalPurchaseAmountByStock = holding.getQuantity() * holding.getAveragePrice();
            Long evaluationAmount = holding.getQuantity() * holding.getStock().getCurrentPrice();
            Long profitAmountByStock = evaluationAmount - totalPurchaseAmountByStock;

            return new HoldingResponse(
                holding.getStock().getId(),
                holding.getStock().getStockCode(),
                holding.getStock().getStockName(),
                holding.getQuantity(),
                holding.getAveragePrice(),
                holding.getStock().getCurrentPrice(),
                totalPurchaseAmountByStock,
                evaluationAmount,
                profitAmountByStock,
                totalPurchaseAmountByStock == 0 ? 0.0 :
                    profitAmountByStock.doubleValue() / totalPurchaseAmountByStock.doubleValue() * 100
            );
          })
          .toList();

      return new AccountMeResponse(
          account.getCashBalance(),
          totalPurchaseAmount,
          stockEvaluationAmount,
          totalAssetAmount,
          profitAmount,
          profitRate,
          holdingResponseList
      );
    }

    @Transactional
    public AccountInitResponse initMyAccount(Long userId, AccountInitRequest request) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

      if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
      }

      Account account = accountRepository.findByUserIdForUpdate(userId)
          .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

      holdingRepository.deleteAllByAccount_Id(account.getId());
      tradeRepository.deleteAllByAccount_Id(account.getId());

      account.resetCash();

      return new AccountInitResponse(account.getCashBalance(), 0L);
    }
}
