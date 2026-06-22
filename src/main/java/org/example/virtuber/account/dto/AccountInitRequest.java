package org.example.virtuber.account.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountInitRequest(
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    String password
) {
}
