package com.demo.accountapi.infrastructure.persistence.repository;

import com.demo.accountapi.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
    Page<AccountEntity> findByUserId(UUID userId, Pageable pageable);
}
