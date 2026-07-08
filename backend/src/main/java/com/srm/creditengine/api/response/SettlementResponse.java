package com.srm.creditengine.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        AssignorResponse assignor,
        String sourceCurrency,
        String paymentCurrency,
        String status,
        BigDecimal baseRate,
        Integer itemCount,
        BigDecimal totalFaceValue,
        BigDecimal totalPresentValue,
        BigDecimal totalPaymentValue,
        Instant settledAt,
        List<SettlementItemResponse> items
) {
}
