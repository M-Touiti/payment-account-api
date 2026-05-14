package com.demo.accountapi.application.port.out;

import com.demo.accountapi.domain.model.Transaction;
import com.demo.accountapi.domain.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    Page<Transaction> findByAccountId(UUID accountId, Pageable pageable);
    Page<Transaction> findByAccountIdAndType(UUID accountId, TransactionType type, Pageable pageable);
}
