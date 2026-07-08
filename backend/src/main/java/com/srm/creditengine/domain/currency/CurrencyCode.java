package com.srm.creditengine.domain.currency;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.BadRequestException;
import java.util.List;

public record CurrencyCode(String value) {

    public CurrencyCode {
        validate(value, "currency");
    }

    public static CurrencyCode of(String value) {
        return new CurrencyCode(value);
    }

    public static CurrencyCode of(String value, String field) {
        validate(value, field);
        return new CurrencyCode(value);
    }

    private static void validate(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(
                    "Currency code is required.",
                    List.of(new ApiErrorDetail(field, "Currency code is required."))
            );
        }
        if (!value.matches("[A-Z]{3}")) {
            throw new BadRequestException(
                    "Currency code must use the ISO-like three-letter uppercase format.",
                    List.of(new ApiErrorDetail(field, "Currency code must match [A-Z]{3}."))
            );
        }
    }
}
