package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.shared.Money;
import com.srm.creditengine.domain.shared.Rate;
import com.srm.creditengine.domain.shared.Term;
import java.time.Instant;

public record PricingContext(
        Money faceValue,
        CurrencyCode paymentCurrency,
        Rate baseRate,
        String receivableType,
        Term term,
        Rate exchangeRate,
        Instant calculatedAt
) {
}
