package com.srm.creditengine.application.exchange;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExchangeRateResult(
    UUID id,
    String sourceCurrency,
    String targetCurrency,
    BigDecimal rate,
    Instant validAt,
    Instant createdAt) {}
