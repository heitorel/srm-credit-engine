package com.srm.creditengine.application.statement;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.BadRequestException;
import java.util.List;

public class InvalidPaginationException extends BadRequestException {

  public InvalidPaginationException(String field, String message) {
    super("Invalid pagination parameters.", List.of(new ApiErrorDetail(field, message)));
  }
}
