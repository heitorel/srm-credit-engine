package com.srm.creditengine.domain.pricing;

import java.math.BigDecimal;
import java.time.Instant;

public record PricingResult(
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
        Instant calculatedAt
) {
}
