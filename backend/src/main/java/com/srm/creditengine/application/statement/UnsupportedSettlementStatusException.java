package com.srm.creditengine.application.statement;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.util.List;

public class UnsupportedSettlementStatusException extends UnprocessableEntityException {

  public UnsupportedSettlementStatusException(String rawStatus) {
    super(
        "Unsupported settlement status: " + rawStatus + ".",
        List.of(
            new ApiErrorDetail(
                "status",
                "Supported settlement statuses are PENDING, SETTLED, FAILED and CANCELLED.")));
  }
}
