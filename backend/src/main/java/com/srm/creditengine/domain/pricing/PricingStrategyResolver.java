package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.receivable.ReceivableType;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PricingStrategyResolver {

  private final List<PricingStrategy> strategies;

  public PricingStrategyResolver(List<PricingStrategy> strategies) {
    this.strategies = List.copyOf(strategies);
  }

  public PricingStrategy resolve(String rawReceivableType) {
    return resolve(ReceivableType.from(rawReceivableType));
  }

  public PricingStrategy resolve(ReceivableType receivableType) {
    return strategies.stream()
        .filter(strategy -> strategy.supports(receivableType))
        .findFirst()
        .orElseThrow(() -> new UnsupportedReceivableTypeException(receivableType.name()));
  }
}
