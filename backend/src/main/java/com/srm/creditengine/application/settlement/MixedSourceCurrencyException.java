package com.srm.creditengine.application.settlement;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.util.List;

public class MixedSourceCurrencyException extends UnprocessableEntityException {

  public MixedSourceCurrencyException() {
    super(
        "All receivables in the settlement batch must share the same source currency.",
        List.of(
            new ApiErrorDetail(
                "receivables",
                "Mixed source currencies are not allowed in the same settlement batch.")));
  }
}
