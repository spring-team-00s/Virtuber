import { formatRate, formatWon } from "../utils/formatters";

function AccountSummary({ account }) {
  return (
    <div className="card">
      <h2>내 계좌</h2>

      {account ? (
        <div className="account-list">
          <p>현금 잔고: {formatWon(account.cashBalance)}</p>
          <p>총 매수금액: {formatWon(account.totalPurchaseAmount)}</p>
          <p>주식 평가금액: {formatWon(account.stockEvaluationAmount)}</p>
          <p>총 자산: {formatWon(account.totalAssetAmount)}</p>
          <p className={account.profitAmount >= 0 ? "profit plus" : "profit minus"}>
            평가손익: {formatWon(account.profitAmount)} /{" "}
            {formatRate(account.profitRate)}
          </p>
        </div>
      ) : (
        <p className="empty">로그인 후 계좌 정보를 조회할 수 있습니다.</p>
      )}
    </div>
  );
}

export default AccountSummary;
