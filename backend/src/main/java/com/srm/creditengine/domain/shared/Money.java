package com.srm.creditengine.domain.shared;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.currency.CurrencyCode;
import java.math.BigDecimal;
import java.util.List;

public record Money(BigDecimal amount, CurrencyCode currency) {

    public Money {
        if (amount == null) {
            throw new BadRequestException(
                    "Money amount is required.",
                    List.of(new ApiErrorDetail("amount", "Money amount is required."))
            );
        }
        if (currency == null) {
            throw new BadRequestException(
                    "Currency is required.",
                    List.of(new ApiErrorDetail("currency", "Currency is required."))
            );
        }
    }

    public static Money positive(BigDecimal amount, CurrencyCode currency, String field, String label) {
        Money money = new Money(amount, currency);
        if (money.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    label + " must be greater than zero.",
                    List.of(new ApiErrorDetail(field, label + " must be greater than zero."))
            );
        }
        return money;
    }
}
