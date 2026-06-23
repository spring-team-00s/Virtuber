import {
  formatRate,
  formatWon,
  getTradeTypeLabel,
} from "../utils/formatters";

function TradeResult({ result }) {
  if (!result) return null;

  return (
    <div className="result-box">
      <h3>{getTradeTypeLabel(result.tradeType)} 완료</h3>
      <p>종목: {result.stockName}</p>
      <p>수량: {result.quantity}주</p>
      <p>거래가: {formatWon(result.price)}</p>
      <p>총 거래금액: {formatWon(result.totalAmount)}</p>
      <p>거래 후 현금 잔고: {formatWon(result.cashBalance)}</p>

      {result.tradeType === "SELL" && (
        <div className="sell-profit-box">
          <p>매도 전 평균매수가: {formatWon(result.averagePriceBeforeSell)}</p>
          <p
            className={
              result.sellProfitAmount >= 0 ? "profit plus" : "profit minus"
            }
          >
            판매수익: {formatWon(result.sellProfitAmount)}
          </p>
          <p
            className={
              result.sellProfitRate >= 0 ? "profit plus" : "profit minus"
            }
          >
            수익률: {formatRate(result.sellProfitRate)}
          </p>
        </div>
      )}
    </div>
  );
}

export default TradeResult;
