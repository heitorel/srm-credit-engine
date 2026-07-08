package com.srm.creditengine.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ExchangeRateResponse(
        UUID id,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal rate,
        Instant validAt,
        Instant createdAt
) {
}
