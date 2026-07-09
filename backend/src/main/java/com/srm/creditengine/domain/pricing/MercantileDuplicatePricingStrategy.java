package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.receivable.ReceivableType;
import com.srm.creditengine.domain.shared.Rate;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class MercantileDuplicatePricingStrategy implements PricingStrategy {

  private static final Rate SPREAD = new Rate(new BigDecimal("0.01500000"));

  @Override
  public boolean supports(ReceivableType receivableType) {
    return receivableType == ReceivableType.MERCANTILE_DUPLICATE;
  }

  @Override
  public Rate spread() {
    return SPREAD;
  }
}
