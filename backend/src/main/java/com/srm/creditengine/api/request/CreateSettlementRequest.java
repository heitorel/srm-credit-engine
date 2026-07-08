package com.srm.creditengine.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateSettlementRequest(
        @NotNull(message = "Assignor is required.")
        @Valid
        AssignorRequest assignor,
        @NotBlank(message = "Payment currency is required.")
        String paymentCurrency,
        @DecimalMin(value = "0.00000000", inclusive = true, message = "Base rate must be greater than or equal to zero.")
        BigDecimal baseRate,
        @NotNull(message = "Receivables list is required.")
        @Size(min = 1, max = 100, message = "Receivables list must contain between 1 and 100 items.")
        @Valid
        List<ReceivableSettlementRequest> receivables
) {
}
