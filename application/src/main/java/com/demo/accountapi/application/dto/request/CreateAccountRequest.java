package com.demo.accountapi.application.dto.request;

import com.demo.accountapi.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(@NotNull AccountType type, @NotBlank String currency) {}
