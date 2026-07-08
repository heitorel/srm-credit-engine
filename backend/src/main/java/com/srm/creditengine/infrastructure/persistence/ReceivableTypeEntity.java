package com.srm.creditengine.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receivable_types")
public class ReceivableTypeEntity {

    @Id
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "description", nullable = false, length = 128)
    private String description;

    @Column(name = "monthly_spread", nullable = false, precision = 19, scale = 8)
    private BigDecimal monthlySpread;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;
}
