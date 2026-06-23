import { formatWon } from "../utils/formatters";

function StockInfoModal({ onClose, stock }) {
  if (!stock) return null;

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <section
        className="stock-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="stockInfoTitle"
        onClick={(event) => event.stopPropagation()}
      >
        <header className="modal-header">
          <div>
            <h2 id="stockInfoTitle">{stock.stockName}</h2>
            <p>{stock.stockCode}</p>
          </div>
          <button className="icon-button" type="button" onClick={onClose}>
            닫기
          </button>
        </header>

        <div className="modal-price-row">
          <div>
            <span>현재가</span>
            <strong>{formatWon(stock.currentPrice)}</strong>
          </div>
          <div>
            <span>상한가</span>
            <strong>{formatWon(stock.upPrice)}</strong>
          </div>
          <div>
            <span>하한가</span>
            <strong>{formatWon(stock.lowPrice)}</strong>
          </div>
        </div>

        <div className="modal-section">
          <h3>회사 정보</h3>
          <p>{stock.companyInfo || "등록된 회사 정보가 없습니다."}</p>
        </div>

        <div className="modal-section">
          <h3>재무 정보</h3>
          <p>{stock.financialInfo || "등록된 재무 정보가 없습니다."}</p>
        </div>
      </section>
    </div>
  );
}

export default StockInfoModal;
