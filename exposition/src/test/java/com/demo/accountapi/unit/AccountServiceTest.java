package com.demo.accountapi.unit;

import com.demo.accountapi.application.dto.request.CreateAccountRequest;
import com.demo.accountapi.application.dto.response.AccountResponse;
import com.demo.accountapi.application.port.out.AccountRepositoryPort;
import com.demo.accountapi.application.service.AccountService;
import com.demo.accountapi.domain.exception.AccountNotFoundException;
import com.demo.accountapi.domain.exception.UnauthorizedAccessException;
import com.demo.accountapi.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepositoryPort accountRepository;

    private AccountService accountService;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
        testUser = new User.Builder().id(UUID.randomUUID()).email("user@test.com")
                .passwordHash("hash").role(Role.USER)
                .createdAt(LocalDateTime.now()).enabled(true).build();
        adminUser = new User.Builder().id(UUID.randomUUID()).email("admin@test.com")
                .passwordHash("hash").role(Role.ADMIN)
                .createdAt(LocalDateTime.now()).enabled(true).build();
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        CreateAccountRequest request = new CreateAccountRequest(AccountType.CHECKING, "EUR");
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.create(request, testUser);

        assertThat(response.type()).isEqualTo("CHECKING");
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.userId()).isEqualTo(testUser.getId());
        verify(accountRepository).save(any());
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(accountRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(unknownId, testUser))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void shouldThrowWhenUserAccessesAnotherUsersAccount() {
        Account otherAccount = Account.createNew(UUID.randomUUID(), AccountType.SAVINGS, "USD");
        when(accountRepository.findById(otherAccount.getId())).thenReturn(Optional.of(otherAccount));

        assertThatThrownBy(() -> accountService.getById(otherAccount.getId(), testUser))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void adminShouldAccessAnyAccount() {
        Account anyAccount = Account.createNew(UUID.randomUUID(), AccountType.SAVINGS, "USD");
        when(accountRepository.findById(anyAccount.getId())).thenReturn(Optional.of(anyAccount));

        AccountResponse response = accountService.getById(anyAccount.getId(), adminUser);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(anyAccount.getId());
    }

    @Test
    void shouldSuspendAccountSuccessfully() {
        Account account = Account.createNew(testUser.getId(), AccountType.CHECKING, "EUR");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.suspend(account.getId(), testUser);

        assertThat(response.status()).isEqualTo("SUSPENDED");
    }

    @Test
    void nonAdminCannotCloseAccount() {
        Account account = Account.createNew(testUser.getId(), AccountType.CHECKING, "EUR");
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.close(account.getId(), testUser))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("admin");
    }
}
