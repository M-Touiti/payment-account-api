package com.demo.accountapi.infrastructure.persistence.adapter;

import com.demo.accountapi.application.port.out.AccountRepositoryPort;
import com.demo.accountapi.domain.model.Account;
import com.demo.accountapi.domain.model.AccountStatus;
import com.demo.accountapi.domain.model.AccountType;
import com.demo.accountapi.infrastructure.persistence.entity.AccountEntity;
import com.demo.accountapi.infrastructure.persistence.repository.AccountJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository jpa;

    public AccountRepositoryAdapter(AccountJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Account save(Account a) { return toDomain(jpa.save(toEntity(a))); }

    @Override
    public Optional<Account> findById(UUID id) { return jpa.findById(id).map(this::toDomain); }

    @Override
    public Page<Account> findByUserId(UUID userId, Pageable pageable) {
        return jpa.findByUserId(userId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Account> findAll(Pageable pageable) {
        return jpa.findAll(pageable).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) { jpa.deleteById(id); }

    private AccountEntity toEntity(Account a) {
        AccountEntity e = new AccountEntity();
        e.setId(a.getId()); e.setUserId(a.getUserId());
        e.setAccountNumber(a.getAccountNumber());
        e.setType(AccountEntity.AccountTypeEntity.valueOf(a.getType().name()));
        e.setCurrency(a.getCurrency()); e.setBalance(a.getBalance());
        e.setStatus(AccountEntity.AccountStatusEntity.valueOf(a.getStatus().name()));
        e.setCreatedAt(a.getCreatedAt()); e.setUpdatedAt(a.getUpdatedAt());
        return e;
    }

    private Account toDomain(AccountEntity e) {
        return new Account.Builder()
                .id(e.getId()).userId(e.getUserId()).accountNumber(e.getAccountNumber())
                .type(AccountType.valueOf(e.getType().name())).currency(e.getCurrency())
                .balance(e.getBalance()).status(AccountStatus.valueOf(e.getStatus().name()))
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }
}
