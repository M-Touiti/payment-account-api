package com.demo.accountapi.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final UUID accountId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String description;
    private final BigDecimal balanceAfter;
    private final LocalDateTime createdAt;

    private Transaction(Builder builder) {
        this.id = builder.id;
        this.accountId = builder.accountId;
        this.type = builder.type;
        this.amount = builder.amount;
        this.description = builder.description;
        this.balanceAfter = builder.balanceAfter;
        this.createdAt = builder.createdAt;
    }

    public static Transaction record(UUID accountId, TransactionType type,
                                     BigDecimal amount, String description,
                                     BigDecimal balanceAfter) {
        return new Builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .type(type)
                .amount(amount)
                .description(description)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class Builder {
        private UUID id;
        private UUID accountId;
        private TransactionType type;
        private BigDecimal amount;
        private String description;
        private BigDecimal balanceAfter;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder accountId(UUID accountId) { this.accountId = accountId; return this; }
        public Builder type(TransactionType type) { this.type = type; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder balanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Transaction build() { return new Transaction(this); }
    }
}
