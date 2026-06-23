function AuthCard({
  currentUserId,
  isLoggedIn,
  isResetOpen,
  onCancelReset,
  onLogout,
  onOpenReset,
  onResetAccount,
  onSignin,
  onSignup,
  password,
  resetPassword,
  setPassword,
  setResetPassword,
  setUserId,
  userId,
}) {
  return (
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
                <button
                  className="danger-button"
                  type="button"
                  onClick={onResetAccount}
                >
                  초기화 실행
                </button>
                <button
                  className="outline-button"
                  type="button"
                  onClick={onCancelReset}
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
                onClick={onOpenReset}
              >
                계좌 초기화
              </button>
            )}
            <button className="outline-button" type="button" onClick={onLogout}>
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
            <button type="button" onClick={onSignin}>
              로그인
            </button>
            <button className="secondary-button" type="button" onClick={onSignup}>
              회원가입
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default AuthCard;
