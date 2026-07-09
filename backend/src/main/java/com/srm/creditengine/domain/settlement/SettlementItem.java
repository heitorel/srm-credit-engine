package com.srm.creditengine.domain.settlement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementItem(
    UUID id,
    UUID receivableId,
    String externalReference,
    String receivableType,
    BigDecimal faceValue,
    String sourceCurrency,
    String paymentCurrency,
    BigDecimal baseRate,
    BigDecimal spread,
    BigDecimal termInMonths,
    BigDecimal presentValueInSourceCurrency,
    BigDecimal discountValue,
    BigDecimal paymentValue,
    BigDecimal exchangeRate,
    Instant calculatedAt) {}
