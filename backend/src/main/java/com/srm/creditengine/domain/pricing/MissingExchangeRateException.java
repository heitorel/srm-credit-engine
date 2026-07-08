package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.util.List;

public class MissingExchangeRateException extends UnprocessableEntityException {

    public MissingExchangeRateException(String sourceCurrency, String targetCurrency) {
        super(
                "Missing exchange rate for currency pair %s -> %s.".formatted(sourceCurrency, targetCurrency),
                List.of(new ApiErrorDetail(
                        "paymentCurrency",
                        "Exchange rate %s -> %s is required for cross-currency operation."
                                .formatted(sourceCurrency, targetCurrency)
                ))
        );
    }
}
