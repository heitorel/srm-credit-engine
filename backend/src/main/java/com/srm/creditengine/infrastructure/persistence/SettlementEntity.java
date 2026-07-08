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
        name = "settlements",
        indexes = {
                @Index(name = "idx_settlements_assignor", columnList = "assignor_id"),
                @Index(name = "idx_settlements_source_currency", columnList = "source_currency_code"),
                @Index(name = "idx_settlements_payment_currency", columnList = "payment_currency_code"),
                @Index(name = "idx_settlements_status", columnList = "status"),
                @Index(name = "idx_settlements_settled_at", columnList = "settled_at"),
                @Index(name = "idx_settlements_statement_default", columnList = "settled_at,id")
        }
)
public class SettlementEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignor_id", nullable = false, columnDefinition = "CHAR(36)")
    private AssignorEntity assignor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_currency_code", nullable = false, columnDefinition = "CHAR(3)")
    private CurrencyEntity sourceCurrency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_currency_code", nullable = false, columnDefinition = "CHAR(3)")
    private CurrencyEntity paymentCurrency;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "base_rate", nullable = false, precision = 19, scale = 8)
    private BigDecimal baseRate;

    @Column(name = "item_count", nullable = false)
    private Integer itemCount;

    @Column(name = "total_face_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalFaceValue;

    @Column(name = "total_present_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPresentValue;

    @Column(name = "total_payment_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPaymentValue;

    @Column(name = "settled_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime settledAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;
}
