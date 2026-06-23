import { formatRate, formatWon } from "../utils/formatters";
import OrderPanel from "./OrderPanel";
import StockPriceChart from "./StockPriceChart";
import TradeResult from "./TradeResult";

function StockDetail({
  canTrade,
  isChartLoading,
  lastTradeResult,
  onBuy,
  onSell,
  onShowInfo,
  orderAmount,
  priceHistories,
  quantity,
  selectedHolding,
  selectedStock,
  selectedStockId,
  setQuantity,
  setSelectedStockId,
  stocks,
}) {
  return (
    <div className="card">
      <h2>종목 상세</h2>

      {selectedStock ? (
        <>
          <div className="stock-detail-head">
            <div>
              <strong>{selectedStock.stockName}</strong>
              <span>{selectedStock.stockCode}</span>
            </div>
            <em>{formatWon(selectedStock.currentPrice)}</em>
          </div>

          <div className="metric-grid">
            <div>
              <span>상한가</span>
              <strong>{formatWon(selectedStock.upPrice)}</strong>
            </div>
            <div>
              <span>하한가</span>
              <strong>{formatWon(selectedStock.lowPrice)}</strong>
            </div>
            <div>
              <span>보유 수량</span>
              <strong>{selectedHolding ? `${selectedHolding.quantity}주` : "0주"}</strong>
            </div>
            <div>
              <span>평균 매수가</span>
              <strong>{formatWon(selectedHolding?.averagePrice)}</strong>
            </div>
          </div>

          <div className="price-chart-section">
            <div className="chart-title-row">
              <h3>최근 주가 그래프</h3>
              <span>최대 20개 기록</span>
            </div>

            {isChartLoading && priceHistories.length === 0 ? (
              <div className="chart-empty">그래프 데이터를 불러오는 중입니다.</div>
            ) : (
              <StockPriceChart histories={priceHistories} />
            )}
          </div>

          {selectedHolding && (
            <div className="holding-summary">
              <span>내 평가손익</span>
              <strong
                className={
                  selectedHolding.profitAmount >= 0
                    ? "profit plus"
                    : "profit minus"
                }
              >
                {formatWon(selectedHolding.profitAmount)} /{" "}
                {formatRate(selectedHolding.profitRate)}
              </strong>
            </div>
          )}

          <button
            className="info-button"
            type="button"
            onClick={() => onShowInfo(selectedStock)}
          >
            기업 정보 보기
          </button>
        </>
      ) : (
        <p className="empty">종목을 선택하면 상세 정보가 표시됩니다.</p>
      )}

      <OrderPanel
        canTrade={canTrade}
        onBuy={onBuy}
        onSell={onSell}
        orderAmount={orderAmount}
        quantity={quantity}
        selectedStockId={selectedStockId}
        setQuantity={setQuantity}
        setSelectedStockId={setSelectedStockId}
        stocks={stocks}
      />

      <TradeResult result={lastTradeResult} />
    </div>
  );
}

export default StockDetail;
