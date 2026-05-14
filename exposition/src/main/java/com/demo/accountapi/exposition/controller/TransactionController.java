package com.demo.accountapi.exposition.controller;

import com.demo.accountapi.application.dto.request.CreateTransactionRequest;
import com.demo.accountapi.application.dto.response.TransactionResponse;
import com.demo.accountapi.application.port.out.UserRepositoryPort;
import com.demo.accountapi.application.service.TransactionService;
import com.demo.accountapi.domain.model.TransactionType;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/transactions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transactions", description = "Credit, debit, and list account transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepositoryPort userRepository;

    public TransactionController(TransactionService transactionService,
                                  UserRepositoryPort userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Create a CREDIT or DEBIT transaction on an account")
    public ResponseEntity<TransactionResponse> create(
            @PathVariable UUID accountId,
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveUser(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(accountId, request, user));
    }

    @GetMapping
    @Operation(summary = "List transactions for an account (paginated, filterable by type)")
    public ResponseEntity<Page<TransactionResponse>> list(
            @PathVariable UUID accountId,
            @RequestParam(required = false) TransactionType type,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        User user = resolveUser(userDetails);
        return ResponseEntity.ok(transactionService.getByAccount(accountId, type, user, pageable));
    }

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }
}
