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

    protected SettlementEntity() {
    }

    private SettlementEntity(
            String id,
            AssignorEntity assignor,
            CurrencyEntity sourceCurrency,
            CurrencyEntity paymentCurrency,
            String status,
            BigDecimal baseRate,
            Integer itemCount,
            BigDecimal totalFaceValue,
            BigDecimal totalPresentValue,
            BigDecimal totalPaymentValue,
            LocalDateTime settledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.assignor = assignor;
        this.sourceCurrency = sourceCurrency;
        this.paymentCurrency = paymentCurrency;
        this.status = status;
        this.baseRate = baseRate;
        this.itemCount = itemCount;
        this.totalFaceValue = totalFaceValue;
        this.totalPresentValue = totalPresentValue;
        this.totalPaymentValue = totalPaymentValue;
        this.settledAt = settledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SettlementEntity create(
            String id,
            AssignorEntity assignor,
            CurrencyEntity sourceCurrency,
            CurrencyEntity paymentCurrency,
            String status,
            BigDecimal baseRate,
            Integer itemCount,
            BigDecimal totalFaceValue,
            BigDecimal totalPresentValue,
            BigDecimal totalPaymentValue,
            LocalDateTime settledAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new SettlementEntity(
                id,
                assignor,
                sourceCurrency,
                paymentCurrency,
                status,
                baseRate,
                itemCount,
                totalFaceValue,
                totalPresentValue,
                totalPaymentValue,
                settledAt,
                createdAt,
                updatedAt
        );
    }

    public String getId() {
        return id;
    }

    public AssignorEntity getAssignor() {
        return assignor;
    }

    public CurrencyEntity getSourceCurrency() {
        return sourceCurrency;
    }

    public CurrencyEntity getPaymentCurrency() {
        return paymentCurrency;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getBaseRate() {
        return baseRate;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public BigDecimal getTotalFaceValue() {
        return totalFaceValue;
    }

    public BigDecimal getTotalPresentValue() {
        return totalPresentValue;
    }

    public BigDecimal getTotalPaymentValue() {
        return totalPaymentValue;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
