package org.example.virtuber.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.virtuber.account.AccountRepository;
import org.example.virtuber.account.entity.Account;
import org.example.virtuber.auth.dto.AuthResponse;
import org.example.virtuber.auth.dto.RegisterRequest;
import org.example.virtuber.auth.dto.SigninRequest;
import org.example.virtuber.auth.dto.SigninResponse;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.security.JwtTokenProvider;
import org.example.virtuber.user.entity.User;
import org.example.virtuber.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    Long seedMoney = 10_000_000L;

    if (userRepository.existsByUserId(request.userId())) {
      throw new BusinessException(ErrorCode.DUPLICATE_ID);
    }

    User user = new User(
        request.userId(),
        passwordEncoder.encode(request.password())
    );
    User savedUser = userRepository.save(user);

    Account account = new Account(user, seedMoney);
    Account savedAccount = accountRepository.save(account);

    return new AuthResponse(
        user.getId(),
        user.getUserId(),
        account.getCashBalance()
    );
  }

  @Transactional
  public SigninResponse signin(SigninRequest request) {
    User user = userRepository.findByUserId(request.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken = jwtTokenProvider.createAccessToken(user.getId());

    return new SigninResponse(
            user.getId(),
            user.getUserId(),
            accessToken
    );
  }
}
