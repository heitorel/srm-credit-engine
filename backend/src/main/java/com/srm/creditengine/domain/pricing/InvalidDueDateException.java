package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.util.List;

public class InvalidDueDateException extends UnprocessableEntityException {

  public InvalidDueDateException() {
    super(
        "Receivable due date must be in the future.",
        List.of(new ApiErrorDetail("dueDate", "Due date must be after pricing date.")));
  }
}
