package org.example.virtuber.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SignoutRequest(
        @NotBlank(message = "Refresh Token은 필수 항목입니다.")
        String refreshToken
) {
}
