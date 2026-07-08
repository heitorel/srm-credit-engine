package com.srm.creditengine.application.statement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementStatementRow(
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
