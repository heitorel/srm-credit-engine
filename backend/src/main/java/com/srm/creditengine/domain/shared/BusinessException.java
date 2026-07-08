package com.srm.creditengine.domain.shared;

import com.srm.creditengine.api.error.ApiErrorDetail;
import java.util.List;
import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final List<ApiErrorDetail> details;

    protected BusinessException(HttpStatus status, String message) {
        this(status, message, List.of());
    }

    protected BusinessException(HttpStatus status, String message, List<ApiErrorDetail> details) {
        super(message);
        this.status = status;
        this.details = List.copyOf(details);
    }

    public HttpStatus status() {
        return status;
    }

    public List<ApiErrorDetail> details() {
        return details;
    }
}
