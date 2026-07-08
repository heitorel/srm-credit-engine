package com.srm.creditengine.domain.shared;

import com.srm.creditengine.api.error.ApiErrorDetail;
import java.util.List;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String message, List<ApiErrorDetail> details) {
        super(HttpStatus.NOT_FOUND, message, details);
    }
}
