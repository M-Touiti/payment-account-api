package com.demo.accountapi.domain.model;

import com.demo.accountapi.domain.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final UUID userId;
    private final String accountNumber;
    private final AccountType type;
    private final String currency;
    private BigDecimal balance;
    private AccountStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Account(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.accountNumber = builder.accountNumber;
        this.type = builder.type;
        this.currency = builder.currency;
        this.balance = builder.balance;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Account createNew(UUID userId, AccountType type, String currency) {
        return new Builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .accountNumber(generateAccountNumber())
                .type(type)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Business methods
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        if (!AccountStatus.ACTIVE.equals(status)) {
            throw new IllegalStateException("Cannot credit a non-active account");
        }
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (!AccountStatus.ACTIVE.equals(status)) {
            throw new IllegalStateException("Cannot debit a non-active account");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds: balance=" + balance + ", requested=" + amount);
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() { return AccountStatus.ACTIVE.equals(status); }
    public boolean belongsTo(UUID userId) { return this.userId.equals(userId); }

    private static String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getAccountNumber() { return accountNumber; }
    public AccountType getType() { return type; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private String accountNumber;
        private AccountType type;
        private String currency;
        private BigDecimal balance;
        private AccountStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder accountNumber(String n) { this.accountNumber = n; return this; }
        public Builder type(AccountType type) { this.type = type; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder balance(BigDecimal balance) { this.balance = balance; return this; }
        public Builder status(AccountStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Account build() { return new Account(this); }
    }
}
