package com.demo.accountapi.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = @Index(name = "idx_transactions_account_id", columnList = "account_id"))
public class TransactionEntity {

    @Id private UUID id;
    @Column(name = "account_id", nullable = false) private UUID accountId;
    @Column(nullable = false) @Enumerated(EnumType.STRING) private TransactionTypeEntity type;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column(nullable = false) private String description;
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4) private BigDecimal balanceAfter;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    public enum TransactionTypeEntity { CREDIT, DEBIT }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }
    public TransactionTypeEntity getType() { return type; }
    public void setType(TransactionTypeEntity type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
