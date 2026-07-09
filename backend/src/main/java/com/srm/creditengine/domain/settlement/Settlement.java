package com.srm.creditengine.domain.settlement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Settlement(
    UUID id,
    Assignor assignor,
    String sourceCurrency,
    String paymentCurrency,
    SettlementStatus status,
    BigDecimal baseRate,
    Integer itemCount,
    BigDecimal totalFaceValue,
    BigDecimal totalPresentValue,
    BigDecimal totalPaymentValue,
    Instant settledAt,
    List<SettlementItem> items) {}
