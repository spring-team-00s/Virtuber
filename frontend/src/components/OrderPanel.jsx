import { formatWon } from "../utils/formatters";

function OrderPanel({
  canTrade,
  onBuy,
  onSell,
  orderAmount,
  quantity,
  selectedStockId,
  setQuantity,
  setSelectedStockId,
  stocks,
}) {
  return (
    <div className="order-panel">
      <h3>주문</h3>

      <label htmlFor="stockId">종목</label>
      <select
        id="stockId"
        value={selectedStockId}
        onChange={(event) => setSelectedStockId(event.target.value)}
      >
        {stocks.map((stock) => (
          <option key={stock.stockId} value={stock.stockId}>
            {stock.stockName} - {formatWon(stock.currentPrice)}
          </option>
        ))}
      </select>

      <label htmlFor="quantity">수량</label>
      <input
        id="quantity"
        type="number"
        min="1"
        value={quantity}
        onChange={(event) => setQuantity(event.target.value)}
      />

      <div className="order-total">
        <span>예상 주문금액</span>
        <strong>{formatWon(orderAmount)}</strong>
      </div>

      <div className="button-row">
        <button type="button" disabled={!canTrade} onClick={onBuy}>
          매수
        </button>
        <button
          className="danger-button"
          type="button"
          disabled={!canTrade}
          onClick={onSell}
        >
          매도
        </button>
      </div>
    </div>
  );
}

export default OrderPanel;
