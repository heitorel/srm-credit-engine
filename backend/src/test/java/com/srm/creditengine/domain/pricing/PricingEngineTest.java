package com.srm.creditengine.domain.pricing;

import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.shared.FinancialMath;
import com.srm.creditengine.domain.shared.Money;
import com.srm.creditengine.domain.shared.Rate;
import com.srm.creditengine.domain.shared.Term;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricingEngineTest {

    private final FinancialMath financialMath = new FinancialMath();
    private final PricingStrategyResolver resolver = new PricingStrategyResolver(
            java.util.List.of(new MercantileDuplicatePricingStrategy(), new PostDatedCheckPricingStrategy())
    );
    private final PricingEngine pricingEngine = new PricingEngine(
            resolver,
            new CurrencyConversionService(financialMath),
            financialMath
    );

    @Test
    void shouldCalculatePresentValueForSameCurrencyOperation() {
        PricingResult result = pricingEngine.price(context("BRL", null));

        assertThat(result.presentValueInSourceCurrency()).isEqualByComparingTo("9518.14");
    }

    @Test
    void shouldCalculateDiscountAsFaceValueMinusPresentValue() {
        PricingResult result = pricingEngine.price(context("BRL", null));

        assertThat(result.discountValue()).isEqualByComparingTo("481.86");
    }

    @Test
    void shouldUsePresentValueAsPaymentValueWhenSameCurrency() {
        PricingResult result = pricingEngine.price(context("BRL", null));

        assertThat(result.netPaymentValue()).isEqualByComparingTo(result.presentValueInSourceCurrency());
        assertThat(result.exchangeRate()).isNull();
    }

    @Test
    void shouldApplyExchangeConversionAfterPresentValueCalculation() {
        PricingResult result = pricingEngine.price(context("USD", new Rate(new BigDecimal("0.19000000"))));

        assertThat(result.presentValueInSourceCurrency()).isEqualByComparingTo("9518.14");
        assertThat(result.netPaymentValue()).isEqualByComparingTo("1808.45");
        assertThat(result.exchangeRate()).isEqualByComparingTo("0.19000000");
    }

    @Test
    void shouldFailCrossCurrencyPricingWhenExchangeRateIsMissing() {
        assertThatThrownBy(() -> pricingEngine.price(context("USD", null)))
                .isInstanceOf(MissingExchangeRateException.class)
                .hasMessage("Missing exchange rate for currency pair BRL -> USD.");
    }

    @Test
    void shouldNotRequireExchangeRateForSameCurrencyPricing() {
        PricingResult result = pricingEngine.price(context("BRL", null));

        assertThat(result.exchangeRate()).isNull();
    }

    @Test
    void shouldReturnExchangeRateUsedInPricingResult() {
        PricingResult result = pricingEngine.price(context("USD", new Rate(new BigDecimal("0.19000000"))));

        assertThat(result.exchangeRate()).isEqualByComparingTo("0.19000000");
    }

    @Test
    void shouldRoundUsingConfiguredPolicy() {
        assertThat(financialMath.roundMoney(new BigDecimal("123.455"))).isEqualByComparingTo("123.46");
        assertThat(financialMath.roundRate(new BigDecimal("0.015000001"))).isEqualByComparingTo("0.01500000");
    }

    private PricingContext context(String paymentCurrency, Rate exchangeRate) {
        return new PricingContext(
                Money.positive(new BigDecimal("10000.00"), CurrencyCode.of("BRL"), "faceValue", "Face value"),
                CurrencyCode.of(paymentCurrency),
                Rate.nonNegative(new BigDecimal("0.01000000"), "baseRate", "Base rate"),
                "MERCANTILE_DUPLICATE",
                new Term(new BigDecimal("2.00000000")),
                exchangeRate,
                Instant.parse("2026-07-07T13:20:00Z")
        );
    }
}
