package com.srm.creditengine.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "settlement_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_settlement_items_receivable",
                columnNames = "receivable_id"
        ),
        indexes = {
                @Index(name = "idx_settlement_items_settlement", columnList = "settlement_id"),
                @Index(name = "idx_settlement_items_receivable_type", columnList = "receivable_type_code"),
                @Index(name = "idx_settlement_items_source_currency", columnList = "source_currency_code"),
                @Index(name = "idx_settlement_items_payment_currency", columnList = "payment_currency_code"),
                @Index(name = "idx_settlement_items_calculated_at", columnList = "calculated_at")
        }
)
public class SettlementItemEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false, columnDefinition = "CHAR(36)")
    private SettlementEntity settlement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receivable_id", nullable = false, columnDefinition = "CHAR(36)")
    private ReceivableEntity receivable;

    @Column(name = "external_reference", nullable = false, length = 128)
    private String externalReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receivable_type_code", nullable = false)
    private ReceivableTypeEntity receivableType;

    @Column(name = "face_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal faceValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_currency_code", nullable = false, columnDefinition = "CHAR(3)")
    private CurrencyEntity sourceCurrency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_currency_code", nullable = false, columnDefinition = "CHAR(3)")
    private CurrencyEntity paymentCurrency;

    @Column(name = "base_rate", nullable = false, precision = 19, scale = 8)
    private BigDecimal baseRate;

    @Column(name = "spread", nullable = false, precision = 19, scale = 8)
    private BigDecimal spread;

    @Column(name = "term_in_months", nullable = false, precision = 19, scale = 8)
    private BigDecimal termInMonths;

    @Column(name = "present_value_source", nullable = false, precision = 19, scale = 4)
    private BigDecimal presentValueSource;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountValue;

    @Column(name = "payment_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal paymentValue;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "calculated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime calculatedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;
}
