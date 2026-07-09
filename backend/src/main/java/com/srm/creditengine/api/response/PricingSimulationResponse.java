package com.srm.creditengine.api.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PricingSimulationResponse(
    BigDecimal faceValue,
    String sourceCurrency,
    String paymentCurrency,
    BigDecimal presentValueInSourceCurrency,
    BigDecimal netPaymentValue,
    BigDecimal discountValue,
    BigDecimal baseRate,
    BigDecimal spread,
    BigDecimal termInMonths,
    BigDecimal exchangeRate,
    Instant calculatedAt) {}
