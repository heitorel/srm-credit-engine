package com.srm.creditengine.application.settlement;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.util.List;

public class InvalidSettlementBatchException extends UnprocessableEntityException {

    public InvalidSettlementBatchException(String message, List<ApiErrorDetail> details) {
        super(message, details);
    }
}
