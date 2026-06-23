export const formatWon = (value) => {
  if (value === null || value === undefined) return "-";
  return `${Number(value).toLocaleString()}원`;
};

export const formatRate = (value) => {
  if (value === null || value === undefined) return "-";
  return `${Number(value).toFixed(2)}%`;
};

export const getTradeTypeLabel = (tradeType) =>
  tradeType === "SELL" ? "매도" : "매수";
