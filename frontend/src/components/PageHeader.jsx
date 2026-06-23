function PageHeader({ isLoggedIn, lastUpdatedAt }) {
  return (
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
  );
}

export default PageHeader;
