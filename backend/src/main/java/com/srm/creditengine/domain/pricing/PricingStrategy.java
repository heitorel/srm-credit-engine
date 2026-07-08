package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.receivable.ReceivableType;
import com.srm.creditengine.domain.shared.Rate;

public interface PricingStrategy {

    boolean supports(ReceivableType receivableType);

    Rate spread();
}
