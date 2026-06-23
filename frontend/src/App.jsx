import { useEffect, useState } from "react";
import "./App.css";
import {
  buyStock,
  getMyAccount,
  getStocks,
  getStockPriceHistories,
  getTrades,
  resetAccount,
  sellStock,
  signin,
  signup,
} from "./api/virtuberApi";

function StockPriceChart({ histories, formatWon }) {
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

  if (!histories || histories.length === 0) {
    return (
        <div className="chart-empty">
          아직 기록된 주가가 없습니다. 가격 스케줄러가 한 번 실행되면 그래프가 표시됩니다.
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

function App() {
  const savedUserId = localStorage.getItem("userId") || "";
  const [userId, setUserId] = useState("");
  const [password, setPassword] = useState("");
  const [resetPassword, setResetPassword] = useState("");
  const [isResetOpen, setIsResetOpen] = useState(false);

  const [isLoggedIn, setIsLoggedIn] = useState(
    Boolean(localStorage.getItem("accessToken")),
  );
  const [currentUserId, setCurrentUserId] = useState(savedUserId);
  const [stocks, setStocks] = useState([]);
  const [account, setAccount] = useState(null);
  const [trades, setTrades] = useState([]);
  const [priceHistories, setPriceHistories] = useState([]);
  const [isChartLoading, setIsChartLoading] = useState(false);

  const [selectedStockId, setSelectedStockId] = useState("");
  const [quantity, setQuantity] = useState(1);
  const [lastTradeResult, setLastTradeResult] = useState(null);
  const [infoStock, setInfoStock] = useState(null);

  const [toast, setToast] = useState(null);
  const [lastUpdatedAt, setLastUpdatedAt] = useState(null);

  const canTrade = isLoggedIn && selectedStockId && Number(quantity) > 0;
  const selectedStock = stocks.find(
    (stock) => stock.stockId === Number(selectedStockId),
  );
  const selectedHolding = account?.holdings?.find(
    (holding) => holding.stockId === Number(selectedStockId),
  );
  const orderAmount = selectedStock ? selectedStock.currentPrice * Number(quantity || 0) : 0;

  const formatWon = (value) => {
    if (value === null || value === undefined) return "-";
    return `${Number(value).toLocaleString()}원`;
  };

  const formatRate = (value) => {
    if (value === null || value === undefined) return "-";
    return `${Number(value).toFixed(2)}%`;
  };

  const getTradeTypeLabel = (tradeType) => (tradeType === "SELL" ? "매도" : "매수");

  const showToast = (text, type = "success") => {
    setToast({ text, type });
  };

  const getErrorMessage = (error, fallback) => {
    const responseError = error.response?.data?.error;
    const detailMessages = responseError?.details
      ?.map((detail) => detail.message)
      .filter(Boolean);

    if (detailMessages?.length > 0) {
      return detailMessages.join(" ");
    }

    return responseError?.message || error.response?.data?.message || fallback;
  };

  const loadStocks = async () => {
    try {
      const data = await getStocks();
      const stockList = data || [];

      setStocks(stockList);
      if (stockList.length > 0) {
        setSelectedStockId((currentStockId) => currentStockId || stockList[0].stockId);
      }
      setLastUpdatedAt(new Date());
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "종목 목록 조회 실패"), "error");
    }
  };

  const loadPriceHistories = async (stockId = selectedStockId) => {
    if (!stockId) {
      setPriceHistories([]);
      return;
    }

    try {
      setIsChartLoading(true);
      const data = await getStockPriceHistories(stockId);
      setPriceHistories(data || []);
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "주가 그래프 조회 실패"), "error");
    } finally {
      setIsChartLoading(false);
    }
  };

  const loadAccount = async () => {
    try {
      const data = await getMyAccount();
      setAccount(data);
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "내 계좌 조회 실패"), "error");
    }
  };

  const loadTrades = async () => {
    try {
      const data = await getTrades();
      setTrades(data || []);
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "거래내역 조회 실패"), "error");
    }
  };

  const refreshPrivateData = async () => {
    await Promise.all([loadAccount(), loadTrades()]);
  };

  const handleSignup = async () => {
    if (!userId.trim() || !password.trim()) {
      showToast("아이디와 비밀번호를 직접 입력해 주세요.", "error");
      return;
    }

    try {
      await signup({ userId, password });
      showToast("회원가입 성공. 이제 로그인하면 됩니다.");
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "회원가입 실패"), "error");
    }
  };

  const handleSignin = async () => {
    if (!userId.trim() || !password.trim()) {
      showToast("아이디와 비밀번호를 직접 입력해 주세요.", "error");
      return;
    }

    try {
      const data = await signin({ userId, password });

      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);
      localStorage.setItem("userId", data.userId);

      setIsLoggedIn(true);
      setCurrentUserId(data.userId);
      setPassword("");
      showToast(`${data.userId} 로그인 성공`);

      await refreshPrivateData();
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "로그인 실패"), "error");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("userId");
    setIsLoggedIn(false);
    setCurrentUserId("");
    setAccount(null);
    setTrades([]);
    setPriceHistories([]);
    setResetPassword("");
    setIsResetOpen(false);
    setLastTradeResult(null);
    showToast("로그아웃 완료");
  };

  const handleResetAccount = async () => {
    if (!resetPassword.trim()) {
      showToast("계좌 초기화를 위해 비밀번호를 입력해 주세요.", "error");
      return;
    }

    const confirmed = window.confirm(
      "계좌를 초기화하면 보유 주식과 거래 내역이 삭제됩니다. 계속할까요?",
    );

    if (!confirmed) return;

    try {
      await resetAccount({ password: resetPassword });
      setResetPassword("");
      setIsResetOpen(false);
      setLastTradeResult(null);
      showToast("계좌가 초기화되었습니다.");

      await refreshPrivateData();
      await loadStocks();
      await loadPriceHistories(selectedStockId);
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "계좌 초기화 실패"), "error");
    }
  };

  const handleBuy = async () => {
    try {
      const result = await buyStock({
        stockId: Number(selectedStockId),
        quantity: Number(quantity),
      });

      setLastTradeResult(result);
      showToast("매수 완료");

      await refreshPrivateData();
      await loadStocks();
      await loadPriceHistories(selectedStockId);
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "매수 실패"), "error");
    }
  };

  const handleSell = async () => {
    try {
      const result = await sellStock({
        stockId: Number(selectedStockId),
        quantity: Number(quantity),
      });

      setLastTradeResult(result);
      showToast("매도 완료");

      await refreshPrivateData();
      await loadStocks();
      await loadPriceHistories(selectedStockId);
    } catch (error) {
      console.error(error);
      showToast(getErrorMessage(error, "매도 실패"), "error");
    }
  };

  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    loadStocks();

    const token = localStorage.getItem("accessToken");
    if (token) {
      refreshPrivateData();
    }
    // This screen intentionally loads API data once on mount for manual testing.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  /* eslint-enable react-hooks/set-state-in-effect */

  useEffect(() => {
    const intervalId = window.setInterval(async () => {
      await loadStocks();

      if (selectedStockId) {
        await loadPriceHistories(selectedStockId);
      }

      if (isLoggedIn) {
        await loadAccount();
      }
    }, 5000);

    return () => window.clearInterval(intervalId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoggedIn, selectedStockId]);

  useEffect(() => {
    if (!toast) return undefined;

    const timeoutId = window.setTimeout(() => {
      setToast(null);
    }, 4000);

    return () => window.clearTimeout(timeoutId);
  }, [toast]);

  useEffect(() => {
    if (!selectedStockId) {
      setPriceHistories([]);
      return;
    }

    loadPriceHistories(selectedStockId);

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedStockId]);

  return (
    <main className="page">
      <header className="header">
        <div>
          <h1>Virtuber 모의투자</h1>
          <p>Spring API 시연용 간단 대시보드</p>
          <p className="refresh-status">
            가격 자동 갱신 중
            {lastUpdatedAt && ` · ${lastUpdatedAt.toLocaleTimeString()}`}
          </p>
        </div>

        {isLoggedIn && <span className="login-badge">로그인 중</span>}
      </header>

      {toast && (
        <div className={`toast toast-${toast.type}`} role="alert">
          <span>{toast.text}</span>
          <button type="button" onClick={() => setToast(null)}>
            닫기
          </button>
        </div>
      )}

      <section className="grid">
        <div className="card">
          {isLoggedIn ? (
            <>
              <h2>계정 관리</h2>
              <div className="signed-in-box">
                <span className="status-dot" />
                <div>
                  <strong>{currentUserId || "로그인 사용자"}</strong>
                  <p>거래 가능한 상태입니다.</p>
                </div>
              </div>

              {isResetOpen && (
                <div className="reset-panel">
                  <label htmlFor="resetPassword">비밀번호 확인</label>
                  <input
                    id="resetPassword"
                    type="password"
                    value={resetPassword}
                    onChange={(event) => setResetPassword(event.target.value)}
                    placeholder="현재 비밀번호"
                  />

                  <div className="button-row">
                    <button className="danger-button" type="button" onClick={handleResetAccount}>
                      초기화 실행
                    </button>
                    <button
                      className="outline-button"
                      type="button"
                      onClick={() => {
                        setResetPassword("");
                        setIsResetOpen(false);
                      }}
                    >
                      취소
                    </button>
                  </div>
                </div>
              )}

              <div className="button-row">
                {!isResetOpen && (
                  <button
                    className="danger-button"
                    type="button"
                    onClick={() => setIsResetOpen(true)}
                  >
                    계좌 초기화
                  </button>
                )}
                <button className="outline-button" type="button" onClick={handleLogout}>
                  로그아웃
                </button>
              </div>
            </>
          ) : (
            <>
              <h2>로그인 / 회원가입</h2>

              <label htmlFor="userId">아이디</label>
              <input
                id="userId"
                value={userId}
                onChange={(event) => setUserId(event.target.value)}
                placeholder="user01"
              />

              <label htmlFor="password">비밀번호</label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Password123!"
              />

              <div className="button-row">
                <button type="button" onClick={handleSignin}>
                  로그인
                </button>
                <button className="secondary-button" type="button" onClick={handleSignup}>
                  회원가입
                </button>
              </div>
            </>
          )}
        </div>

        <div className="card">
          <h2>내 계좌</h2>

          {account ? (
            <div className="account-list">
              <p>현금 잔고: {formatWon(account.cashBalance)}</p>
              <p>총 매수금액: {formatWon(account.totalPurchaseAmount)}</p>
              <p>주식 평가금액: {formatWon(account.stockEvaluationAmount)}</p>
              <p>총 자산: {formatWon(account.totalAssetAmount)}</p>
              <p className={account.profitAmount >= 0 ? "profit plus" : "profit minus"}>
                평가손익: {formatWon(account.profitAmount)} / {formatRate(account.profitRate)}
              </p>
            </div>
          ) : (
            <p className="empty">로그인 후 계좌 정보를 조회할 수 있습니다.</p>
          )}
        </div>
      </section>

      <section className="grid">
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
                    <StockPriceChart histories={priceHistories} formatWon={formatWon} />
                )}
              </div>

              {selectedHolding && (
                <div className="holding-summary">
                  <span>내 평가손익</span>
                  <strong
                    className={
                      selectedHolding.profitAmount >= 0 ? "profit plus" : "profit minus"
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
                onClick={() => setInfoStock(selectedStock)}
              >
                기업 정보 보기
              </button>
            </>
          ) : (
            <p className="empty">종목을 선택하면 상세 정보가 표시됩니다.</p>
          )}

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
              <button type="button" disabled={!canTrade} onClick={handleBuy}>
                매수
              </button>
              <button
                className="danger-button"
                type="button"
                disabled={!canTrade}
                onClick={handleSell}
              >
                매도
              </button>
            </div>
          </div>

          {lastTradeResult && (
            <div className="result-box">
              <h3>{getTradeTypeLabel(lastTradeResult.tradeType)} 완료</h3>
              <p>종목: {lastTradeResult.stockName}</p>
              <p>수량: {lastTradeResult.quantity}주</p>
              <p>거래가: {formatWon(lastTradeResult.price)}</p>
              <p>총 거래금액: {formatWon(lastTradeResult.totalAmount)}</p>
              <p>거래 후 현금 잔고: {formatWon(lastTradeResult.cashBalance)}</p>

              {lastTradeResult.tradeType === "SELL" && (
                <div className="sell-profit-box">
                  <p>
                    매도 전 평균매수가:{" "}
                    {formatWon(lastTradeResult.averagePriceBeforeSell)}
                  </p>
                  <p
                    className={
                      lastTradeResult.sellProfitAmount >= 0
                        ? "profit plus"
                        : "profit minus"
                    }
                  >
                    판매수익: {formatWon(lastTradeResult.sellProfitAmount)}
                  </p>
                  <p
                    className={
                      lastTradeResult.sellProfitRate >= 0 ? "profit plus" : "profit minus"
                    }
                  >
                    수익률: {formatRate(lastTradeResult.sellProfitRate)}
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      </section>

      <section className="card">
        <h2>보유 주식</h2>

        {account?.holdings?.length > 0 ? (
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
                {account.holdings.map((holding) => (
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

      {infoStock && (
        <div className="modal-backdrop" role="presentation" onClick={() => setInfoStock(null)}>
          <section
            className="stock-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="stockInfoTitle"
            onClick={(event) => event.stopPropagation()}
          >
            <header className="modal-header">
              <div>
                <h2 id="stockInfoTitle">{infoStock.stockName}</h2>
                <p>{infoStock.stockCode}</p>
              </div>
              <button className="icon-button" type="button" onClick={() => setInfoStock(null)}>
                닫기
              </button>
            </header>

            <div className="modal-price-row">
              <div>
                <span>현재가</span>
                <strong>{formatWon(infoStock.currentPrice)}</strong>
              </div>
              <div>
                <span>상한가</span>
                <strong>{formatWon(infoStock.upPrice)}</strong>
              </div>
              <div>
                <span>하한가</span>
                <strong>{formatWon(infoStock.lowPrice)}</strong>
              </div>
            </div>

            <div className="modal-section">
              <h3>회사 정보</h3>
              <p>{infoStock.companyInfo || "등록된 회사 정보가 없습니다."}</p>
            </div>

            <div className="modal-section">
              <h3>재무 정보</h3>
              <p>{infoStock.financialInfo || "등록된 재무 정보가 없습니다."}</p>
            </div>
          </section>
        </div>
      )}
    </main>
  );
}

export default App;
