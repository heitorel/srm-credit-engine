package com.srm.creditengine.application.pricing;

import com.srm.creditengine.api.request.PricingSimulationRequest;
import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.pricing.PricingContext;
import com.srm.creditengine.domain.pricing.PricingEngine;
import com.srm.creditengine.domain.pricing.PricingResult;
import com.srm.creditengine.domain.shared.Money;
import com.srm.creditengine.domain.shared.Rate;
import com.srm.creditengine.domain.shared.Term;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.repository.ExchangeRateJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingSimulationService {

    private final PricingEngine pricingEngine;
    private final ListReferenceDataService referenceDataService;
    private final ExchangeRateJpaRepository exchangeRateRepository;
    private final BaseRateResolver baseRateResolver;
    private final Clock clock;

    public PricingSimulationService(
            PricingEngine pricingEngine,
            ListReferenceDataService referenceDataService,
            ExchangeRateJpaRepository exchangeRateRepository,
            BaseRateResolver baseRateResolver,
            Clock clock
    ) {
        this.pricingEngine = pricingEngine;
        this.referenceDataService = referenceDataService;
        this.exchangeRateRepository = exchangeRateRepository;
        this.baseRateResolver = baseRateResolver;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PricingResult simulate(PricingSimulationRequest request) {
        CurrencyCode sourceCurrency = referenceDataService.requireSupportedCurrency(
                request.sourceCurrency(),
                "sourceCurrency"
        );
        CurrencyCode paymentCurrency = referenceDataService.requireSupportedCurrency(
                request.paymentCurrency(),
                "paymentCurrency"
        );

        Money faceValue = Money.positive(request.faceValue(), sourceCurrency, "faceValue", "Face value");
        Rate baseRate = baseRateResolver.resolve(request.baseRate());
        LocalDate pricingDate = LocalDate.now(clock);
        Term term = Term.between(pricingDate, request.dueDate());
        Instant calculatedAt = Instant.now(clock);

        Rate exchangeRate = sourceCurrency.value().equals(paymentCurrency.value())
                ? null
                : exchangeRateRepository.findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(
                                sourceCurrency.value(),
                                paymentCurrency.value()
                        )
                        .map(ExchangeRateEntity::toResult)
                        .map(result -> Rate.positive(result.rate(), "exchangeRate", "Exchange rate"))
                        .orElse(null);

        return pricingEngine.price(new PricingContext(
                faceValue,
                paymentCurrency,
                baseRate,
                request.receivableType(),
                term,
                exchangeRate,
                calculatedAt
        ));
    }
}
