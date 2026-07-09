package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.receivable.ReceivableType;
import com.srm.creditengine.domain.shared.FinancialMath;
import com.srm.creditengine.domain.shared.Money;
import org.springframework.stereotype.Component;

@Component
public class PricingEngine {

  private final PricingStrategyResolver pricingStrategyResolver;
  private final CurrencyConversionService currencyConversionService;
  private final FinancialMath financialMath;

  public PricingEngine(
      PricingStrategyResolver pricingStrategyResolver,
      CurrencyConversionService currencyConversionService,
      FinancialMath financialMath) {
    this.pricingStrategyResolver = pricingStrategyResolver;
    this.currencyConversionService = currencyConversionService;
    this.financialMath = financialMath;
  }

  public PricingResult price(PricingContext context) {
    ReceivableType receivableType = ReceivableType.from(context.receivableType());
    PricingStrategy strategy = pricingStrategyResolver.resolve(receivableType);

    Money faceValue = context.faceValue();
    var spread = strategy.spread();

    var rawPresentValue =
        financialMath.calculatePresentValue(
            faceValue.amount(), context.baseRate().value(), spread.value(), context.term().value());
    Money presentValue = new Money(rawPresentValue, faceValue.currency());
    Money netPaymentValue =
        currencyConversionService.convert(
            presentValue, context.paymentCurrency(), context.exchangeRate());
    var discountValue = financialMath.calculateDiscount(faceValue.amount(), rawPresentValue);

    return new PricingResult(
        financialMath.roundMoney(faceValue.amount()),
        faceValue.currency().value(),
        context.paymentCurrency().value(),
        financialMath.roundMoney(rawPresentValue),
        financialMath.roundMoney(netPaymentValue.amount()),
        financialMath.roundMoney(discountValue),
        financialMath.roundRate(context.baseRate().value()),
        financialMath.roundRate(spread.value()),
        financialMath.roundTerm(context.term().value()),
        context.exchangeRate() == null
            ? null
            : financialMath.roundRate(context.exchangeRate().value()),
        context.calculatedAt());
  }
}
