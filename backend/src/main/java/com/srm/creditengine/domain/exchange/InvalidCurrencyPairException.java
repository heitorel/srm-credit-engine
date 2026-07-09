package com.srm.creditengine.domain.exchange;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.BadRequestException;
import java.util.List;

public class InvalidCurrencyPairException extends BadRequestException {

  public InvalidCurrencyPairException() {
    super(
        "Source currency and target currency must be different.",
        List.of(
            new ApiErrorDetail(
                "targetCurrency", "Target currency must be different from source currency.")));
  }
}
