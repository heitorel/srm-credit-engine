package com.srm.creditengine.domain.shared;

import com.srm.creditengine.api.error.ApiErrorDetail;
import java.util.List;
import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends BusinessException {

  public UnprocessableEntityException(String message) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, message);
  }

  public UnprocessableEntityException(String message, List<ApiErrorDetail> details) {
    super(HttpStatus.UNPROCESSABLE_ENTITY, message, details);
  }
}
