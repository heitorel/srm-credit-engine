package com.srm.creditengine.domain.exchange;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.BadRequestException;
import java.util.List;

public class UnsupportedCurrencyException extends BadRequestException {

    public UnsupportedCurrencyException(String field, String currencyCode) {
        super(
                "Unsupported currency: " + currencyCode + ".",
                List.of(new ApiErrorDetail(field, "Supported currencies are BRL and USD."))
        );
    }
}
