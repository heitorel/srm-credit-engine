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

    public static Rate positive(BigDecimal value, String field, String label) {
        Rate rate = new Rate(value);
        if (rate.value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    label + " must be greater than zero.",
                    List.of(new ApiErrorDetail(field, label + " must be greater than zero."))
            );
        }
        return rate;
    }

    public static Rate nonNegative(BigDecimal value, String field, String label) {
        Rate rate = new Rate(value);
        if (rate.value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                    label + " must be greater than or equal to zero.",
                    List.of(new ApiErrorDetail(field, label + " must be greater than or equal to zero."))
            );
        }
        return rate;
    }
}
