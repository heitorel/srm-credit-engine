package com.srm.creditengine.domain.exchange;

import com.srm.creditengine.domain.shared.ResourceNotFoundException;

public class ExchangeRateNotFoundException extends ResourceNotFoundException {

    public ExchangeRateNotFoundException(String sourceCurrency, String targetCurrency) {
        super("No exchange rate found for currency pair %s -> %s.".formatted(sourceCurrency, targetCurrency));
    }
}
