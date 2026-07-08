package com.srm.creditengine.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "exchange_rates",
        indexes = @Index(
                name = "idx_exchange_rates_pair_valid_at",
                columnList = "source_currency_code,target_currency_code,valid_at,created_at"
        )
)
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
}
