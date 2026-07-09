package com.srm.creditengine.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateExchangeRateRequest(
    @NotBlank(message = "Source currency is required.") String sourceCurrency,
    @NotBlank(message = "Target currency is required.") String targetCurrency,
    @NotNull(message = "Rate is required.") @DecimalMin(
            value = "0.00000000",
            inclusive = false,
            message = "Rate must be greater than zero.")
        BigDecimal rate,
    @NotNull(message = "validAt is required.") Instant validAt) {}
