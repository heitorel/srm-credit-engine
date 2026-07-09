package com.srm.creditengine.application.statement;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.BadRequestException;
import java.util.List;

public class InvalidStatementDateRangeException extends BadRequestException {

  public InvalidStatementDateRangeException() {
    super(
        "Invalid statement date range.",
        List.of(new ApiErrorDetail("from", "From date must be less than or equal to to date.")));
  }
}
