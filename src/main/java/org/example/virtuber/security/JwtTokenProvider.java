package org.example.virtuber.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtTokenProvider {
  private final SecretKey secretKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  public JwtTokenProvider(
      @Value("${jwt.secret}") String secret,
      @Value("${jwt.access-token-expiration-ms}") String accessTokenExpirationMs,
      @Value("${jwt.refresh-token-expiration-ms}") String refreshTokenExpirationMs
  ) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationMs = Long.parseLong(accessTokenExpirationMs);
    this.refreshTokenExpirationMs = Long.parseLong(refreshTokenExpirationMs);
  }

  public String createAccessToken(Long userId) {
    Date now = new Date();
    Date expiresAt = new Date(now.getTime() + accessTokenExpirationMs);

    return Jwts.builder()
        .subject(String.valueOf(userId))
        .issuedAt(now)
        .expiration(expiresAt)
        .signWith(secretKey)
        .compact();
  }

  public String createRefreshToken(Long userId) {
    Date now = new Date();
    Date expiresAt = new Date(now.getTime() + refreshTokenExpirationMs);

    return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiration(expiresAt)
            .signWith(secretKey)
            .compact();
  }

  public LocalDateTime getRefreshTokenExpiresAt() {
    return LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs));
  }

  public boolean validateToken(String token) {
    Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token);

    return true;
  }

  public String getUserId(String token) {
    return getClaims(token).getSubject();
  }

  private Claims getClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
