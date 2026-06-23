import {
  formatRate,
  formatWon,
  getTradeTypeLabel,
} from "../utils/formatters";

function TradesTable({ trades }) {
  return (
    <section className="card">
      <h2>거래 내역</h2>

      {trades.length > 0 ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>구분</th>
                <th>종목</th>
                <th>수량</th>
                <th>가격</th>
                <th>총액</th>
                <th>매도 전 평단가</th>
                <th>판매수익</th>
                <th>수익률</th>
              </tr>
            </thead>
            <tbody>
              {trades.map((trade) => (
                <tr key={trade.tradeId}>
                  <td>{getTradeTypeLabel(trade.tradeType)}</td>
                  <td>{trade.stockName}</td>
                  <td>{trade.quantity}</td>
                  <td>{formatWon(trade.price)}</td>
                  <td>{formatWon(trade.totalAmount)}</td>
                  <td>{formatWon(trade.averagePriceBeforeSell)}</td>
                  <td
                    className={
                      trade.sellProfitAmount >= 0 ? "profit plus" : "profit minus"
                    }
                  >
                    {formatWon(trade.sellProfitAmount)}
                  </td>
                  <td
                    className={
                      trade.sellProfitRate >= 0 ? "profit plus" : "profit minus"
                    }
                  >
                    {formatRate(trade.sellProfitRate)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="empty">거래 내역이 없습니다.</p>
      )}
    </section>
  );
}

export default TradesTable;
