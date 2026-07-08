package com.srm.creditengine.domain.shared;

import com.srm.creditengine.api.error.ApiErrorDetail;
import java.math.BigDecimal;
import java.util.List;

public record Rate(BigDecimal value) {

    public Rate {
        if (value == null) {
            throw new BadRequestException(
                    "Rate is required.",
                    List.of(new ApiErrorDetail("rate", "Rate is required."))
            );
        }
    }

    public static Rate positive(BigDecimal value) {
        Rate rate = new Rate(value);
        if (rate.value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Rate must be greater than zero.",
                    List.of(new ApiErrorDetail("rate", "Rate must be greater than zero."))
            );
        }
        return rate;
    }
}
