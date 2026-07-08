package com.srm.creditengine.infrastructure.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementStatementRowProjection(
        String settlementId,
        String assignorId,
        String assignorName,
        String assignorDocument,
        String sourceCurrency,
        String paymentCurrency,
        String status,
        Integer itemCount,
        BigDecimal totalFaceValue,
        BigDecimal totalPresentValue,
        BigDecimal totalPaymentValue,
        LocalDateTime settledAt
) {
}
