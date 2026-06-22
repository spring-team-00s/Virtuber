package org.example.virtuber.account.dto;

public record AccountInitResponse(
    Long cashBalance,
    Long holdingsCount
) {
}
