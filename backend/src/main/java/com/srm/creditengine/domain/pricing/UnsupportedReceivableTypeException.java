package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.util.List;

public class UnsupportedReceivableTypeException extends UnprocessableEntityException {

  public UnsupportedReceivableTypeException(String receivableType) {
    super(
        "Unsupported receivable type: " + receivableType + ".",
        List.of(
            new ApiErrorDetail(
                "receivableType",
                "Supported receivable types are MERCANTILE_DUPLICATE and POST_DATED_CHECK.")));
  }
}
