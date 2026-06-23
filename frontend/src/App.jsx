import { useEffect, useState } from "react";
import "./App.css";
import AccountSummary from "./components/AccountSummary";
import AuthCard from "./components/AuthCard";
import HoldingsTable from "./components/HoldingsTable";
import PageHeader from "./components/PageHeader";
import StockDetail from "./components/StockDetail";
import StockInfoModal from "./components/StockInfoModal";
import StockList from "./components/StockList";
import Toast from "./components/Toast";
import TradesTable from "./components/TradesTable";
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
import { getErrorMessage } from "./utils/errors";

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

  const selectedStock = stocks.find(
    (stock) => stock.stockId === Number(selectedStockId),
  );
  const selectedHolding = account?.holdings?.find(
    (holding) => holding.stockId === Number(selectedStockId),
  );
  const orderAmount = selectedStock
    ? selectedStock.currentPrice * Number(quantity || 0)
    : 0;
  const canTrade = isLoggedIn && selectedStockId && Number(quantity) > 0;

  const showToast = (text, type = "success") => {
    setToast({ text, type });
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

  /* eslint-disable react-hooks/set-state-in-effect */
  useEffect(() => {
    loadPriceHistories(selectedStockId);

    // This screen intentionally syncs chart data when the selected stock changes.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedStockId]);
  /* eslint-enable react-hooks/set-state-in-effect */

  return (
    <main className="page">
      <PageHeader isLoggedIn={isLoggedIn} lastUpdatedAt={lastUpdatedAt} />
      <Toast toast={toast} onClose={() => setToast(null)} />

      <section className="grid">
        <AuthCard
          currentUserId={currentUserId}
          isLoggedIn={isLoggedIn}
          isResetOpen={isResetOpen}
          onCancelReset={() => {
            setResetPassword("");
            setIsResetOpen(false);
          }}
          onLogout={handleLogout}
          onOpenReset={() => setIsResetOpen(true)}
          onResetAccount={handleResetAccount}
          onSignin={handleSignin}
          onSignup={handleSignup}
          password={password}
          resetPassword={resetPassword}
          setPassword={setPassword}
          setResetPassword={setResetPassword}
          setUserId={setUserId}
          userId={userId}
        />
        <AccountSummary account={account} />
      </section>

      <section className="grid">
        <StockList
          selectedStockId={selectedStockId}
          setSelectedStockId={setSelectedStockId}
          stocks={stocks}
        />
        <StockDetail
          canTrade={canTrade}
          isChartLoading={isChartLoading}
          lastTradeResult={lastTradeResult}
          onBuy={handleBuy}
          onSell={handleSell}
          onShowInfo={setInfoStock}
          orderAmount={orderAmount}
          priceHistories={priceHistories}
          quantity={quantity}
          selectedHolding={selectedHolding}
          selectedStock={selectedStock}
          selectedStockId={selectedStockId}
          setQuantity={setQuantity}
          setSelectedStockId={setSelectedStockId}
          stocks={stocks}
        />
      </section>

      <HoldingsTable holdings={account?.holdings || []} />
      <TradesTable trades={trades} />
      <StockInfoModal stock={infoStock} onClose={() => setInfoStock(null)} />
    </main>
  );
}

export default App;
