package com.demo.accountapi.application.port.out;

import com.demo.accountapi.domain.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    Page<Account> findByUserId(UUID userId, Pageable pageable);
    Page<Account> findAll(Pageable pageable);
    void deleteById(UUID id);
}
