package com.demo.accountapi.infrastructure.persistence.repository;

import com.demo.accountapi.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    Page<TransactionEntity> findByAccountId(UUID accountId, Pageable pageable);
    Page<TransactionEntity> findByAccountIdAndType(UUID accountId, TransactionEntity.TransactionTypeEntity type, Pageable pageable);
}
