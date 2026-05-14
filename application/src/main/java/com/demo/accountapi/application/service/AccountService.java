package com.demo.accountapi.application.service;

import com.demo.accountapi.application.dto.request.CreateAccountRequest;
import com.demo.accountapi.application.dto.response.AccountResponse;
import com.demo.accountapi.application.port.out.AccountRepositoryPort;
import com.demo.accountapi.domain.exception.AccountNotFoundException;
import com.demo.accountapi.domain.exception.UnauthorizedAccessException;
import com.demo.accountapi.domain.model.Account;
import com.demo.accountapi.domain.model.Role;
import com.demo.accountapi.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepositoryPort accountRepository;

    public AccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request, User currentUser) {
        Account account = Account.createNew(currentUser.getId(), request.type(), request.currency());
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID accountId, User currentUser) {
        Account account = findAccountOrThrow(accountId);
        assertOwnerOrAdmin(account, currentUser);
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getMyAccounts(User currentUser, Pageable pageable) {
        return accountRepository.findByUserId(currentUser.getId(), pageable)
                .map(AccountResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(AccountResponse::from);
    }

    @Transactional
    public AccountResponse suspend(UUID accountId, User currentUser) {
        Account account = findAccountOrThrow(accountId);
        assertOwnerOrAdmin(account, currentUser);
        account.suspend();
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public void close(UUID accountId, User currentUser) {
        Account account = findAccountOrThrow(accountId);
        if (!currentUser.isAdmin()) {
            throw new UnauthorizedAccessException("Only admins can close accounts");
        }
        account.close();
        accountRepository.save(account);
    }

    public Account findAccountOrThrow(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
    }

    private void assertOwnerOrAdmin(Account account, User user) {
        if (!account.belongsTo(user.getId()) && !user.isAdmin()) {
            throw new UnauthorizedAccessException("Access denied to account: " + account.getId());
        }
    }
}
