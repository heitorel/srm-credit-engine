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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "receivables",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_receivables_assignor_external_reference",
                columnNames = {"assignor_id", "external_reference"}
        ),
        indexes = {
                @Index(name = "idx_receivables_assignor", columnList = "assignor_id"),
                @Index(name = "idx_receivables_type", columnList = "receivable_type_code"),
                @Index(name = "idx_receivables_currency", columnList = "currency_code"),
                @Index(name = "idx_receivables_status", columnList = "status"),
                @Index(name = "idx_receivables_due_date", columnList = "due_date")
        }
)
public class ReceivableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignor_id", nullable = false, columnDefinition = "CHAR(36)")
    private AssignorEntity assignor;

    @Column(name = "external_reference", nullable = false, length = 128)
    private String externalReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receivable_type_code", nullable = false)
    private ReceivableTypeEntity receivableType;

    @Column(name = "face_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal faceValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_code", nullable = false, columnDefinition = "CHAR(3)")
    private CurrencyEntity currency;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;

    protected ReceivableEntity() {
    }

    private ReceivableEntity(
            String id,
            AssignorEntity assignor,
            String externalReference,
            ReceivableTypeEntity receivableType,
            BigDecimal faceValue,
            CurrencyEntity currency,
            LocalDate dueDate,
            String status,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.assignor = assignor;
        this.externalReference = externalReference;
        this.receivableType = receivableType;
        this.faceValue = faceValue;
        this.currency = currency;
        this.dueDate = dueDate;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ReceivableEntity create(
            String id,
            AssignorEntity assignor,
            String externalReference,
            ReceivableTypeEntity receivableType,
            BigDecimal faceValue,
            CurrencyEntity currency,
            LocalDate dueDate,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new ReceivableEntity(
                id,
                assignor,
                externalReference,
                receivableType,
                faceValue,
                currency,
                dueDate,
                status,
                null,
                createdAt,
                updatedAt
        );
    }

    public void markAsSettled(LocalDateTime updatedAt) {
        this.status = "SETTLED";
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public AssignorEntity getAssignor() {
        return assignor;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public ReceivableTypeEntity getReceivableType() {
        return receivableType;
    }

    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
