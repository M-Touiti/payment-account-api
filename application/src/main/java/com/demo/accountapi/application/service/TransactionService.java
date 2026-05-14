package com.demo.accountapi.application.service;

import com.demo.accountapi.application.dto.request.CreateTransactionRequest;
import com.demo.accountapi.application.dto.response.TransactionResponse;
import com.demo.accountapi.application.port.out.TransactionRepositoryPort;
import com.demo.accountapi.domain.model.Account;
import com.demo.accountapi.domain.model.Transaction;
import com.demo.accountapi.domain.model.TransactionType;
import com.demo.accountapi.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountService accountService;

    public TransactionService(TransactionRepositoryPort transactionRepository,
                               AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    @Transactional
    public TransactionResponse create(UUID accountId, CreateTransactionRequest request,
                                      User currentUser) {
        Account account = accountService.findAccountOrThrow(accountId);

        if (TransactionType.CREDIT.equals(request.type())) {
            account.credit(request.amount());
        } else {
            account.debit(request.amount());
        }

        Transaction transaction = Transaction.record(
                accountId, request.type(), request.amount(),
                request.description(), account.getBalance());

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getByAccount(UUID accountId, TransactionType type,
                                                   User currentUser, Pageable pageable) {
        accountService.findAccountOrThrow(accountId); // validates existence
        Page<Transaction> transactions = (type != null)
                ? transactionRepository.findByAccountIdAndType(accountId, type, pageable)
                : transactionRepository.findByAccountId(accountId, pageable);

        return transactions.map(TransactionResponse::from);
    }
}
