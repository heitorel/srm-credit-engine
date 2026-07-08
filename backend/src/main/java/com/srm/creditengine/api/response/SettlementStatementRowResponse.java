package com.srm.creditengine.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementStatementRowResponse(
        UUID settlementId,
        UUID assignorId,
        String assignorName,
        String assignorDocument,
        String sourceCurrency,
        String paymentCurrency,
        String status,
        Integer itemCount,
        BigDecimal totalFaceValue,
        BigDecimal totalPresentValue,
        BigDecimal totalPaymentValue,
        Instant settledAt
) {
}
