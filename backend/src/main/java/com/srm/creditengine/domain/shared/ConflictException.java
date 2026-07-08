package com.srm.creditengine.domain.shared;

import com.srm.creditengine.api.error.ApiErrorDetail;
import java.util.List;
import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

    public ConflictException(String message, List<ApiErrorDetail> details) {
        super(HttpStatus.CONFLICT, message, details);
    }
}
