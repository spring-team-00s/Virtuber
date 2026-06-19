package org.example.virtuber.auth.dto;

public record AuthResponse(
    Long id,
    String userId,
    Long cashBalance
) {
}
