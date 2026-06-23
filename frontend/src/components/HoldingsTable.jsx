import { formatRate, formatWon } from "../utils/formatters";

function HoldingsTable({ holdings = [] }) {
  return (
    <section className="card">
      <h2>보유 주식</h2>

      {holdings.length > 0 ? (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>종목</th>
                <th>수량</th>
                <th>평균매수가</th>
                <th>현재가</th>
                <th>평가금액</th>
                <th>평가손익</th>
                <th>수익률</th>
              </tr>
            </thead>
            <tbody>
              {holdings.map((holding) => (
                <tr key={holding.stockId}>
                  <td>{holding.stockName}</td>
                  <td>{holding.quantity}</td>
                  <td>{formatWon(holding.averagePrice)}</td>
                  <td>{formatWon(holding.currentPrice)}</td>
                  <td>{formatWon(holding.evaluationAmount)}</td>
                  <td
                    className={
                      holding.profitAmount >= 0 ? "profit plus" : "profit minus"
                    }
                  >
                    {formatWon(holding.profitAmount)}
                  </td>
                  <td
                    className={
                      holding.profitRate >= 0 ? "profit plus" : "profit minus"
                    }
                  >
                    {formatRate(holding.profitRate)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="empty">보유 중인 주식이 없습니다.</p>
      )}
    </section>
  );
}

export default HoldingsTable;
