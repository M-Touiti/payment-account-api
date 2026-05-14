package com.demo.accountapi.application.dto.response;

import com.demo.accountapi.domain.model.Account;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.UUID;

public record AccountResponse(UUID id, UUID userId, String accountNumber, String type,
                               String currency, BigDecimal balance, String status,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getUserId(), a.getAccountNumber(),
                a.getType().name(), a.getCurrency(), a.getBalance(),
                a.getStatus().name(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
