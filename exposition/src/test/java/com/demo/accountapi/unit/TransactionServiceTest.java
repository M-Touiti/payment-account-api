package com.demo.accountapi.unit;

import com.demo.accountapi.application.dto.request.CreateTransactionRequest;
import com.demo.accountapi.application.dto.response.TransactionResponse;
import com.demo.accountapi.application.port.out.AccountRepositoryPort;
import com.demo.accountapi.application.port.out.TransactionRepositoryPort;
import com.demo.accountapi.application.service.AccountService;
import com.demo.accountapi.application.service.TransactionService;
import com.demo.accountapi.domain.exception.AccountNotFoundException;
import com.demo.accountapi.domain.exception.InsufficientFundsException;
import com.demo.accountapi.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private AccountRepositoryPort accountRepository;
    @Mock private TransactionRepositoryPort transactionRepository;

    private TransactionService transactionService;
    private User testUser;

    @BeforeEach
    void setUp() {
        AccountService accountService = new AccountService(accountRepository);
        transactionService = new TransactionService(transactionRepository, accountRepository, accountService);
        testUser = new User.Builder()
                .id(UUID.randomUUID()).email("user@test.com")
                .passwordHash("hash").role(Role.USER)
                .createdAt(LocalDateTime.now()).enabled(true).build();
    }

    @Test
    void shouldCreditAccount() {
        Account account = Account.createNew(testUser.getId(), AccountType.CHECKING, "EUR");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateTransactionRequest request = new CreateTransactionRequest(
                TransactionType.CREDIT, new BigDecimal("200.00"), "Initial deposit");

        TransactionResponse response = transactionService.create(account.getId(), request, testUser);

        assertThat(response.type()).isEqualTo("CREDIT");
        assertThat(response.amount()).isEqualByComparingTo("200.00");
        assertThat(response.balanceAfter()).isEqualByComparingTo("200.00");
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldDebitAccount() {
        Account account = Account.createNew(testUser.getId(), AccountType.CHECKING, "EUR");
        account.credit(new BigDecimal("500.00"));
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateTransactionRequest request = new CreateTransactionRequest(
                TransactionType.DEBIT, new BigDecimal("150.00"), "Withdrawal");

        TransactionResponse response = transactionService.create(account.getId(), request, testUser);

        assertThat(response.type()).isEqualTo("DEBIT");
        assertThat(response.amount()).isEqualByComparingTo("150.00");
        assertThat(response.balanceAfter()).isEqualByComparingTo("350.00");
    }

    @Test
    void shouldThrowInsufficientFundsOnDebit() {
        Account account = Account.createNew(testUser.getId(), AccountType.CHECKING, "EUR");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        CreateTransactionRequest request = new CreateTransactionRequest(
                TransactionType.DEBIT, new BigDecimal("100.00"), "Overdraft attempt");

        assertThatThrownBy(() -> transactionService.create(account.getId(), request, testUser))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void shouldThrowWhenAccountNotFoundOnCreate() {
        UUID unknownId = UUID.randomUUID();
        when(accountRepository.findById(unknownId)).thenReturn(Optional.empty());

        CreateTransactionRequest request = new CreateTransactionRequest(
                TransactionType.CREDIT, new BigDecimal("50.00"), "Test");

        assertThatThrownBy(() -> transactionService.create(unknownId, request, testUser))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldListAllTransactionsWithoutTypeFilter() {
        Account account = Account.createNew(testUser.getId(), AccountType.SAVINGS, "USD");
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountId(eq(account.getId()), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        transactionService.getByAccount(account.getId(), null, testUser, pageable);

        verify(transactionRepository).findByAccountId(account.getId(), pageable);
        verify(transactionRepository, never()).findByAccountIdAndType(any(), any(), any());
    }

    @Test
    void shouldListTransactionsFilteredByType() {
        Account account = Account.createNew(testUser.getId(), AccountType.SAVINGS, "USD");
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdAndType(
                eq(account.getId()), eq(TransactionType.CREDIT), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        transactionService.getByAccount(account.getId(), TransactionType.CREDIT, testUser, pageable);

        verify(transactionRepository).findByAccountIdAndType(account.getId(), TransactionType.CREDIT, pageable);
        verify(transactionRepository, never()).findByAccountId(any(), any());
    }
}
