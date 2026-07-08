package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.shared.FinancialMath;
import com.srm.creditengine.domain.shared.Money;
import com.srm.creditengine.domain.shared.Rate;
import org.springframework.stereotype.Component;

@Component
public class CurrencyConversionService {

    private final FinancialMath financialMath;

    public CurrencyConversionService(FinancialMath financialMath) {
        this.financialMath = financialMath;
    }

    public Money convert(Money sourceAmount, CurrencyCode paymentCurrency, Rate exchangeRate) {
        if (sourceAmount.currency().value().equals(paymentCurrency.value())) {
            return new Money(sourceAmount.amount(), paymentCurrency);
        }
        if (exchangeRate == null) {
            throw new MissingExchangeRateException(sourceAmount.currency().value(), paymentCurrency.value());
        }

        return new Money(
                financialMath.convert(sourceAmount.amount(), exchangeRate.value()),
                paymentCurrency
        );
    }
}
