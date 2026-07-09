package com.srm.creditengine.application.exchange;

import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.exchange.ExchangeRateNotFoundException;
import com.srm.creditengine.domain.exchange.InvalidCurrencyPairException;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.repository.ExchangeRateJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetLatestExchangeRateService {

  private final ExchangeRateJpaRepository exchangeRateRepository;
  private final ListReferenceDataService referenceDataService;

  public GetLatestExchangeRateService(
      ExchangeRateJpaRepository exchangeRateRepository,
      ListReferenceDataService referenceDataService) {
    this.exchangeRateRepository = exchangeRateRepository;
    this.referenceDataService = referenceDataService;
  }

  @Transactional(readOnly = true)
  public ExchangeRateResult getLatest(String sourceCurrencyRaw, String targetCurrencyRaw) {
    CurrencyCode sourceCurrency =
        referenceDataService.requireSupportedCurrency(sourceCurrencyRaw, "sourceCurrency");
    CurrencyCode targetCurrency =
        referenceDataService.requireSupportedCurrency(targetCurrencyRaw, "targetCurrency");
    if (sourceCurrency.value().equals(targetCurrency.value())) {
      throw new InvalidCurrencyPairException();
    }

    return exchangeRateRepository
        .findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(
            sourceCurrency.value(), targetCurrency.value())
        .map(ExchangeRateEntity::toResult)
        .orElseThrow(
            () ->
                new ExchangeRateNotFoundException(sourceCurrency.value(), targetCurrency.value()));
  }
}
