package com.demo.accountapi.infrastructure.persistence.adapter;

import com.demo.accountapi.application.port.out.TransactionRepositoryPort;
import com.demo.accountapi.domain.model.Transaction;
import com.demo.accountapi.domain.model.TransactionType;
import com.demo.accountapi.infrastructure.persistence.entity.TransactionEntity;
import com.demo.accountapi.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpa;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Transaction save(Transaction t) { return toDomain(jpa.save(toEntity(t))); }

    @Override
    public Page<Transaction> findByAccountId(UUID accountId, Pageable pageable) {
        return jpa.findByAccountId(accountId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Transaction> findByAccountIdAndType(UUID accountId, TransactionType type, Pageable pageable) {
        return jpa.findByAccountIdAndType(accountId,
                TransactionEntity.TransactionTypeEntity.valueOf(type.name()), pageable)
                .map(this::toDomain);
    }

    private TransactionEntity toEntity(Transaction t) {
        TransactionEntity e = new TransactionEntity();
        e.setId(t.getId()); e.setAccountId(t.getAccountId());
        e.setType(TransactionEntity.TransactionTypeEntity.valueOf(t.getType().name()));
        e.setAmount(t.getAmount()); e.setDescription(t.getDescription());
        e.setBalanceAfter(t.getBalanceAfter()); e.setCreatedAt(t.getCreatedAt());
        return e;
    }

    private Transaction toDomain(TransactionEntity e) {
        return new Transaction.Builder()
                .id(e.getId()).accountId(e.getAccountId())
                .type(TransactionType.valueOf(e.getType().name()))
                .amount(e.getAmount()).description(e.getDescription())
                .balanceAfter(e.getBalanceAfter()).createdAt(e.getCreatedAt()).build();
    }
}
