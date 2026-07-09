package com.srm.creditengine.infrastructure.persistence;

import com.srm.creditengine.application.exchange.CreateExchangeRateService;
import com.srm.creditengine.application.exchange.ExchangeRateResult;
import com.srm.creditengine.domain.exchange.ExchangeRate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
    name = "exchange_rates",
    indexes =
        @Index(
            name = "idx_exchange_rates_pair_valid_at",
            columnList = "source_currency_code,target_currency_code,valid_at,created_at"))
public class ExchangeRateEntity {

  @Id
  @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_currency_code", nullable = false, columnDefinition = "CHAR(3)")
  private CurrencyEntity sourceCurrency;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_currency_code", nullable = false, columnDefinition = "CHAR(3)")
  private CurrencyEntity targetCurrency;

  @Column(name = "rate", nullable = false, precision = 19, scale = 8)
  private BigDecimal rate;

  @Column(name = "valid_at", nullable = false, columnDefinition = "DATETIME(6)")
  private LocalDateTime validAt;

  @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
  private LocalDateTime createdAt;

  protected ExchangeRateEntity() {}

  private ExchangeRateEntity(
      String id,
      CurrencyEntity sourceCurrency,
      CurrencyEntity targetCurrency,
      BigDecimal rate,
      LocalDateTime validAt,
      LocalDateTime createdAt) {
    this.id = id;
    this.sourceCurrency = sourceCurrency;
    this.targetCurrency = targetCurrency;
    this.rate = rate;
    this.validAt = validAt;
    this.createdAt = createdAt;
  }

  public static ExchangeRateEntity of(
      ExchangeRate exchangeRate, CurrencyEntity sourceCurrency, CurrencyEntity targetCurrency) {
    return new ExchangeRateEntity(
        exchangeRate.id().toString(),
        sourceCurrency,
        targetCurrency,
        exchangeRate.rate().value(),
        CreateExchangeRateService.toUtcDateTime(exchangeRate.validAt()),
        CreateExchangeRateService.toUtcDateTime(exchangeRate.createdAt()));
  }

  public static ExchangeRateResult toResult(ExchangeRateEntity entity) {
    return new ExchangeRateResult(
        UUID.fromString(entity.id),
        entity.sourceCurrency.getCode(),
        entity.targetCurrency.getCode(),
        entity.rate,
        toInstant(entity.validAt),
        toInstant(entity.createdAt));
  }

  public String getId() {
    return id;
  }

  public CurrencyEntity getSourceCurrency() {
    return sourceCurrency;
  }

  public CurrencyEntity getTargetCurrency() {
    return targetCurrency;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public LocalDateTime getValidAt() {
    return validAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  private static Instant toInstant(LocalDateTime value) {
    return value.toInstant(ZoneOffset.UTC);
  }
}
