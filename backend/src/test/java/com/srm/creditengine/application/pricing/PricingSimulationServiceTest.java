package com.srm.creditengine.application.pricing;

import com.srm.creditengine.api.request.PricingSimulationRequest;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.pricing.CurrencyConversionService;
import com.srm.creditengine.domain.pricing.MercantileDuplicatePricingStrategy;
import com.srm.creditengine.domain.pricing.MissingExchangeRateException;
import com.srm.creditengine.domain.pricing.PostDatedCheckPricingStrategy;
import com.srm.creditengine.domain.pricing.PricingEngine;
import com.srm.creditengine.domain.pricing.PricingStrategyResolver;
import com.srm.creditengine.domain.pricing.UnsupportedReceivableTypeException;
import com.srm.creditengine.domain.shared.FinancialMath;
import com.srm.creditengine.infrastructure.repository.ExchangeRateJpaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingSimulationServiceTest {

    private final ListReferenceDataService referenceDataService = Mockito.mock(ListReferenceDataService.class);
    private final ExchangeRateJpaRepository exchangeRateRepository = Mockito.mock(ExchangeRateJpaRepository.class);
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-07-07T13:20:00Z"), ZoneOffset.UTC);
    private final PricingSimulationService service = new PricingSimulationService(
            new PricingEngine(
                    new PricingStrategyResolver(java.util.List.of(
                            new MercantileDuplicatePricingStrategy(),
                            new PostDatedCheckPricingStrategy()
                    )),
                    new CurrencyConversionService(new FinancialMath()),
                    new FinancialMath()
            ),
            referenceDataService,
            exchangeRateRepository,
            new BaseRateResolver(new BigDecimal("0.01000000")),
            fixedClock
    );

    PricingSimulationServiceTest() {
        when(referenceDataService.requireSupportedCurrency("BRL", "sourceCurrency")).thenReturn(CurrencyCode.of("BRL"));
        when(referenceDataService.requireSupportedCurrency("USD", "paymentCurrency")).thenReturn(CurrencyCode.of("USD"));
        when(referenceDataService.requireSupportedCurrency("BRL", "paymentCurrency")).thenReturn(CurrencyCode.of("BRL"));
    }

    @Test
    void shouldSimulatePricingWithoutPersistingSettlement() {
        var result = service.simulate(new PricingSimulationRequest(
                new BigDecimal("10000.00"),
                "BRL",
                "BRL",
                new BigDecimal("0.01000000"),
                "MERCANTILE_DUPLICATE",
                LocalDate.of(2026, 9, 5)
        ));

        assertThat(result.netPaymentValue()).isEqualByComparingTo("9518.14");
        verify(exchangeRateRepository, never())
                .findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(anyString(), anyString());
    }

    @Test
    void shouldFailSimulationWhenExchangeRateIsMissing() {
        when(exchangeRateRepository.findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc("BRL", "USD"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.simulate(new PricingSimulationRequest(
                new BigDecimal("10000.00"),
                "BRL",
                "USD",
                new BigDecimal("0.01000000"),
                "MERCANTILE_DUPLICATE",
                LocalDate.of(2026, 9, 5)
        )))
                .isInstanceOf(MissingExchangeRateException.class);
    }

    @Test
    void shouldFailSimulationForUnsupportedReceivableType() {
        assertThatThrownBy(() -> service.simulate(new PricingSimulationRequest(
                new BigDecimal("10000.00"),
                "BRL",
                "BRL",
                new BigDecimal("0.01000000"),
                "INVOICE",
                LocalDate.of(2026, 9, 5)
        )))
                .isInstanceOf(UnsupportedReceivableTypeException.class);
    }
}
