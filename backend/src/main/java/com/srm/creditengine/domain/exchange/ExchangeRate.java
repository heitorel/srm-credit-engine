package com.srm.creditengine.domain.exchange;

import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.shared.Rate;
import java.time.Instant;
import java.util.UUID;

public record ExchangeRate(
        UUID id,
        CurrencyCode sourceCurrency,
        CurrencyCode targetCurrency,
        Rate rate,
        Instant validAt,
        Instant createdAt
) {

    public ExchangeRate {
        if (id == null) {
            throw new IllegalArgumentException("Exchange rate id is required.");
        }
        if (sourceCurrency == null) {
            throw new IllegalArgumentException("Source currency is required.");
        }
        if (targetCurrency == null) {
            throw new IllegalArgumentException("Target currency is required.");
        }
        if (sourceCurrency.value().equals(targetCurrency.value())) {
            throw new IllegalArgumentException("Source currency and target currency must be different.");
        }
        if (rate == null) {
            throw new IllegalArgumentException("Rate is required.");
        }
        if (validAt == null) {
            throw new IllegalArgumentException("validAt is required.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt is required.");
        }
    }
}
