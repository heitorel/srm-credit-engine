package com.srm.creditengine.application.exchange;

import com.srm.creditengine.api.request.CreateExchangeRateRequest;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.exchange.ExchangeRate;
import com.srm.creditengine.domain.exchange.InvalidCurrencyPairException;
import com.srm.creditengine.domain.shared.Rate;
import com.srm.creditengine.infrastructure.persistence.CurrencyEntity;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.repository.CurrencyJpaRepository;
import com.srm.creditengine.infrastructure.repository.ExchangeRateJpaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateExchangeRateService {

    private final ExchangeRateJpaRepository exchangeRateRepository;
    private final CurrencyJpaRepository currencyRepository;
    private final ListReferenceDataService referenceDataService;
    private final Clock clock;

    public CreateExchangeRateService(
            ExchangeRateJpaRepository exchangeRateRepository,
            CurrencyJpaRepository currencyRepository,
            ListReferenceDataService referenceDataService,
            Clock clock
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
        this.referenceDataService = referenceDataService;
        this.clock = clock;
    }

    @Transactional
    public ExchangeRateResult create(CreateExchangeRateRequest request) {
        CurrencyCode sourceCurrency = referenceDataService.requireSupportedCurrency(
                request.sourceCurrency(),
                "sourceCurrency"
        );
        CurrencyCode targetCurrency = referenceDataService.requireSupportedCurrency(
                request.targetCurrency(),
                "targetCurrency"
        );
        if (sourceCurrency.value().equals(targetCurrency.value())) {
            throw new InvalidCurrencyPairException();
        }

        Instant createdAt = Instant.now(clock);
        ExchangeRate exchangeRate = new ExchangeRate(
                UUID.randomUUID(),
                sourceCurrency,
                targetCurrency,
                Rate.positive(request.rate()),
                request.validAt(),
                createdAt
        );

        CurrencyEntity sourceCurrencyEntity = currencyRepository.getReferenceById(sourceCurrency.value());
        CurrencyEntity targetCurrencyEntity = currencyRepository.getReferenceById(targetCurrency.value());

        ExchangeRateEntity saved = exchangeRateRepository.save(ExchangeRateEntity.of(
                exchangeRate,
                sourceCurrencyEntity,
                targetCurrencyEntity
        ));

        return ExchangeRateEntity.toResult(saved);
    }

    public static LocalDateTime toUtcDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
