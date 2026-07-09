package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.receivable.ReceivableType;
import com.srm.creditengine.domain.shared.Rate;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PostDatedCheckPricingStrategy implements PricingStrategy {

  private static final Rate SPREAD = new Rate(new BigDecimal("0.02500000"));

  @Override
  public boolean supports(ReceivableType receivableType) {
    return receivableType == ReceivableType.POST_DATED_CHECK;
  }

  @Override
  public Rate spread() {
    return SPREAD;
  }
}
