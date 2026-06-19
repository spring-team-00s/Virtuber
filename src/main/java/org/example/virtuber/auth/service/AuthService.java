package org.example.virtuber.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.virtuber.auth.dto.AuthResponse;
import org.example.virtuber.auth.dto.RegisterRequest;
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
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUserId(request.userId())) {
      throw new BusinessException(ErrorCode.DUPLICATE_ID);
    }

    User user = User.create(
        request.userId(),
        passwordEncoder.encode(request.password())
    );

    return new AuthResponse(
        user.getId(),
        user.getUserId(),
        10_000_000L
    );
  }
}
