package com.demo.accountapi.application.dto.response;

import com.demo.accountapi.domain.model.Transaction;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.UUID;

public record TransactionResponse(UUID id, UUID accountId, String type,
                                   BigDecimal amount, String description,
                                   BigDecimal balanceAfter, LocalDateTime createdAt) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAccountId(), t.getType().name(),
                t.getAmount(), t.getDescription(), t.getBalanceAfter(), t.getCreatedAt());
    }
}
