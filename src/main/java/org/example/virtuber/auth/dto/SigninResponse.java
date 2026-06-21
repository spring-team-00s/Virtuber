package org.example.virtuber.auth.dto;

public record SigninResponse(
        Long id,
        String userId,
        String accessToken
) {
}
