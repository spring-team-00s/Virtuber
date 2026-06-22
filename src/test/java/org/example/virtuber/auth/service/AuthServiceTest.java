package org.example.virtuber.auth.service;

import org.example.virtuber.account.entity.Account;
import org.example.virtuber.auth.dto.AuthResponse;
import org.example.virtuber.auth.dto.RegisterRequest;
import org.example.virtuber.auth.dto.SigninRequest;
import org.example.virtuber.auth.dto.SigninResponse;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.support.IntegrationTestSupport;
import org.example.virtuber.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest extends IntegrationTestSupport {

  @Autowired
  AuthService authService;

  @Test
  @DisplayName("회원가입 성공 시 사용자를 저장하고 계좌를 자동 생성한다")
  void registerCreatesUserAndAccount() {
    RegisterRequest request = new RegisterRequest("zero", RAW_PASSWORD);

    AuthResponse response = authService.register(request);

    User user = userRepository.findByUserId("zero").orElseThrow();
    Account account = accountRepository.findByUser_Id(user.getId()).orElseThrow();

    assertThat(response.id()).isEqualTo(user.getId());
    assertThat(response.userId()).isEqualTo("zero");
    assertThat(response.cashBalance()).isEqualTo(10_000_000L);

    assertThat(passwordEncoder.matches(RAW_PASSWORD, user.getPassword())).isTrue();
    assertThat(account.getUser().getId()).isEqualTo(user.getId());
    assertThat(account.getCashBalance()).isEqualTo(10_000_000L);
    assertThat(account.getSeedMoney()).isEqualTo(10_000_000L);
  }

  @Test
  @DisplayName("중복 ID로 회원가입하면 실패한다")
  void registerWithDuplicateUserIdThrowsException() {
    authService.register(new RegisterRequest("zero", RAW_PASSWORD));

    assertThatThrownBy(() -> authService.register(new RegisterRequest("zero", RAW_PASSWORD)))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.DUPLICATE_ID);
  }

  @Test
  @DisplayName("로그인 성공 시 Access Token과 Refresh Token을 발급한다")
  void signinReturnsTokens() {
    authService.register(new RegisterRequest("zero", RAW_PASSWORD));

    SigninResponse response = authService.signin(new SigninRequest("zero", RAW_PASSWORD));

    assertThat(response.userId()).isEqualTo("zero");
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.refreshToken()).isNotBlank();
    assertThat(refreshTokenRepository.findByUserId(response.id())).isPresent();
  }

  @Test
  @DisplayName("존재하지 않는 ID로 로그인하면 실패한다")
  void signinWithUnknownUserThrowsException() {
    assertThatThrownBy(() -> authService.signin(new SigninRequest("unknown", RAW_PASSWORD)))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
  void signinWithWrongPasswordThrowsException() {
    authService.register(new RegisterRequest("zero", RAW_PASSWORD));

    assertThatThrownBy(() -> authService.signin(new SigninRequest("zero", "wrong-password")))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }
}
