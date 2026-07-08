package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.util.List;

public class MissingBaseRateException extends UnprocessableEntityException {

    public MissingBaseRateException() {
        super(
                "Base rate is required.",
                List.of(new ApiErrorDetail(
                        "baseRate",
                        "Base rate must be provided or configured via DEFAULT_BASE_RATE."
                ))
        );
    }
}
