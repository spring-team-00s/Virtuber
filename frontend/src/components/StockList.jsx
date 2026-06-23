import { formatWon } from "../utils/formatters";

function StockList({ selectedStockId, setSelectedStockId, stocks }) {
  return (
    <div className="card">
      <h2>종목 목록</h2>

      {stocks.length > 0 ? (
        <div className="stock-list">
          {stocks.map((stock) => (
            <button
              key={stock.stockId}
              className={
                Number(selectedStockId) === stock.stockId
                  ? "stock-item active"
                  : "stock-item"
              }
              type="button"
              onClick={() => setSelectedStockId(stock.stockId)}
            >
              <strong>{stock.stockName}</strong>
              <span>{stock.stockCode}</span>
              <em>{formatWon(stock.currentPrice)}</em>
            </button>
          ))}
        </div>
      ) : (
        <p className="empty">종목 목록이 없습니다.</p>
      )}
    </div>
  );
}

export default StockList;
