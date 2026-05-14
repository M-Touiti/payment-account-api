package com.demo.accountapi.exposition.controller;

import com.demo.accountapi.application.dto.request.CreateAccountRequest;
import com.demo.accountapi.application.dto.response.AccountResponse;
import com.demo.accountapi.application.service.AccountService;
import com.demo.accountapi.application.port.out.UserRepositoryPort;
import com.demo.accountapi.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Accounts", description = "Create and manage payment accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserRepositoryPort userRepository;

    public AccountController(AccountService accountService, UserRepositoryPort userRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new account for the authenticated user")
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.create(request, user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID (owner or admin)")
    public ResponseEntity<AccountResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(accountService.getById(id, user));
    }

    @GetMapping("/me")
    @Operation(summary = "Get all accounts of the authenticated user (paginated)")
    public ResponseEntity<Page<AccountResponse>> getMyAccounts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(accountService.getMyAccounts(user, pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts — ADMIN only (paginated)")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend an account (owner or admin)")
    public ResponseEntity<AccountResponse> suspend(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(accountService.suspend(id, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close an account — ADMIN only")
    public ResponseEntity<Void> close(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        accountService.close(id, user);
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
