package com.demo.accountapi.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = @Index(name = "idx_accounts_user_id", columnList = "user_id"))
public class AccountEntity {

    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "account_number", nullable = false, unique = true) private String accountNumber;
    @Column(nullable = false) @Enumerated(EnumType.STRING) private AccountTypeEntity type;
    @Column(nullable = false) private String currency;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal balance;
    @Column(nullable = false) @Enumerated(EnumType.STRING) private AccountStatusEntity status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    public enum AccountTypeEntity { CHECKING, SAVINGS, BUSINESS }
    public enum AccountStatusEntity { ACTIVE, SUSPENDED, CLOSED }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public AccountTypeEntity getType() { return type; }
    public void setType(AccountTypeEntity type) { this.type = type; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public AccountStatusEntity getStatus() { return status; }
    public void setStatus(AccountStatusEntity status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
