import { formatWon } from "../utils/formatters";

const formatChartTime = (value) => {
  if (!value) return "-";

  const parsedDate = new Date(value);

  if (Number.isNaN(parsedDate.getTime())) {
    return String(value).slice(11, 19) || String(value);
  }

  return parsedDate.toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
};

function StockPriceChart({ histories }) {
  if (!histories || histories.length === 0) {
    return (
      <div className="chart-empty">
        아직 기록된 주가가 없습니다. 가격 스케줄러가 한 번 실행되면 그래프가
        표시됩니다.
      </div>
    );
  }

  const width = 640;
  const height = 220;
  const paddingX = 80;
  const paddingY = 26;
  const chartWidth = width - paddingX * 2;
  const chartHeight = height - paddingY * 2;

  const prices = histories.map((history) => Number(history.price));
  const minPrice = Math.min(...prices);
  const maxPrice = Math.max(...prices);
  const priceRange = maxPrice - minPrice;

  const getX = (index) => {
    if (histories.length === 1) {
      return paddingX + chartWidth / 2;
    }

    return paddingX + (index / (histories.length - 1)) * chartWidth;
  };

  const getY = (price) => {
    if (priceRange === 0) {
      return paddingY + chartHeight / 2;
    }

    return paddingY + ((maxPrice - price) / priceRange) * chartHeight;
  };

  const points = histories.map((history, index) => ({
    historyId: history.historyId,
    price: Number(history.price),
    recordedTime: history.recordedTime,
    x: getX(index),
    y: getY(Number(history.price)),
  }));

  const polylinePoints = points.map((point) => `${point.x},${point.y}`).join(" ");
  const firstHistory = histories[0];
  const lastHistory = histories[histories.length - 1];
  const priceDiff = Number(lastHistory.price) - Number(firstHistory.price);
  const priceRate =
    Number(firstHistory.price) === 0
      ? 0
      : (priceDiff / Number(firstHistory.price)) * 100;

  return (
    <div className="chart-box">
      <svg
        className="price-chart"
        viewBox={`0 0 ${width} ${height}`}
        role="img"
        aria-label="최근 주가 라인 차트"
      >
        <line
          className="chart-grid-line"
          x1={paddingX}
          y1={paddingY}
          x2={width - paddingX}
          y2={paddingY}
        />
        <line
          className="chart-grid-line"
          x1={paddingX}
          y1={paddingY + chartHeight / 2}
          x2={width - paddingX}
          y2={paddingY + chartHeight / 2}
        />
        <line
          className="chart-grid-line"
          x1={paddingX}
          y1={height - paddingY}
          x2={width - paddingX}
          y2={height - paddingY}
        />

        <polyline className="chart-line" points={polylinePoints} />

        {points.map((point, index) => (
          <circle
            key={point.historyId || `${point.recordedTime}-${index}`}
            className="chart-point"
            cx={point.x}
            cy={point.y}
            r="4"
          />
        ))}

        <text className="chart-axis-label" x="4" y={paddingY + 4}>
          {formatWon(maxPrice)}
        </text>
        <text className="chart-axis-label" x="4" y={height - paddingY + 4}>
          {formatWon(minPrice)}
        </text>
      </svg>

      <div className="chart-meta">
        <span>{formatChartTime(firstHistory.recordedTime)}</span>
        <strong className={priceDiff >= 0 ? "profit plus" : "profit minus"}>
          {priceDiff >= 0 ? "+" : ""}
          {formatWon(priceDiff)} / {priceRate.toFixed(2)}%
        </strong>
        <span>{formatChartTime(lastHistory.recordedTime)}</span>
      </div>
    </div>
  );
}

export default StockPriceChart;
