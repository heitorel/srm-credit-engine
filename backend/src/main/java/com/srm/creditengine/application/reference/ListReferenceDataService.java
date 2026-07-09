package com.srm.creditengine.application.reference;

import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.exchange.UnsupportedCurrencyException;
import com.srm.creditengine.infrastructure.persistence.CurrencyEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableTypeEntity;
import com.srm.creditengine.infrastructure.repository.CurrencyJpaRepository;
import com.srm.creditengine.infrastructure.repository.ReceivableTypeJpaRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListReferenceDataService {

  private final CurrencyJpaRepository currencyRepository;
  private final ReceivableTypeJpaRepository receivableTypeRepository;

  public ListReferenceDataService(
      CurrencyJpaRepository currencyRepository,
      ReceivableTypeJpaRepository receivableTypeRepository) {
    this.currencyRepository = currencyRepository;
    this.receivableTypeRepository = receivableTypeRepository;
  }

  public List<CurrencyReference> listCurrencies() {
    return currencyRepository.findAll().stream()
        .sorted(Comparator.comparing(CurrencyEntity::getCode))
        .map(
            currency ->
                new CurrencyReference(
                    currency.getCode(), currency.getName(), currency.getDecimalPlaces()))
        .toList();
  }

  public List<ReceivableTypeReference> listReceivableTypes() {
    return receivableTypeRepository.findAll().stream()
        .sorted(Comparator.comparing(ReceivableTypeEntity::getCode))
        .map(
            receivableType ->
                new ReceivableTypeReference(
                    receivableType.getCode(),
                    receivableType.getDescription(),
                    receivableType.getMonthlySpread()))
        .toList();
  }

  public CurrencyCode requireSupportedCurrency(String rawCurrencyCode, String field) {
    CurrencyCode currencyCode = CurrencyCode.of(rawCurrencyCode, field);
    return currencyRepository
        .findById(currencyCode.value())
        .map(currency -> currencyCode)
        .orElseThrow(() -> new UnsupportedCurrencyException(field, rawCurrencyCode));
  }
}
