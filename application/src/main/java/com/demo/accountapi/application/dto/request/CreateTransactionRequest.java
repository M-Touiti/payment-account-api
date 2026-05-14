package com.demo.accountapi.application.dto.request;

import com.demo.accountapi.domain.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTransactionRequest(
    @NotNull TransactionType type,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank String description
) {}
