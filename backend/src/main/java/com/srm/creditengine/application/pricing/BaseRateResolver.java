package com.srm.creditengine.application.pricing;

import com.srm.creditengine.domain.pricing.MissingBaseRateException;
import com.srm.creditengine.domain.shared.Rate;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BaseRateResolver {

    private final BigDecimal defaultBaseRate;

    @Autowired
    public BaseRateResolver(@Value("${application.defaults.base-rate:#{null}}") String defaultBaseRateRaw) {
        this(defaultBaseRateRaw == null || defaultBaseRateRaw.isBlank() ? null : new BigDecimal(defaultBaseRateRaw));
    }

    BaseRateResolver(BigDecimal defaultBaseRate) {
        this.defaultBaseRate = defaultBaseRate;
    }

    public Rate resolve(BigDecimal requestedBaseRate) {
        if (requestedBaseRate != null) {
            return Rate.nonNegative(requestedBaseRate, "baseRate", "Base rate");
        }
        if (defaultBaseRate != null) {
            return Rate.nonNegative(defaultBaseRate, "baseRate", "Base rate");
        }
        throw new MissingBaseRateException();
    }
}
