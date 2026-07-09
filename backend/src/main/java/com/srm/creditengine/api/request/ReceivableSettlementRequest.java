package com.srm.creditengine.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceivableSettlementRequest(
    @NotBlank(message = "External reference is required.") String externalReference,
    @NotNull(message = "Face value is required.") @DecimalMin(
            value = "0.00",
            inclusive = false,
            message = "Face value must be greater than zero.")
        BigDecimal faceValue,
    @NotBlank(message = "Source currency is required.") String sourceCurrency,
    @NotBlank(message = "Receivable type is required.") String receivableType,
    @NotNull(message = "Due date is required.") LocalDate dueDate) {}
