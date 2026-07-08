package com.srm.creditengine.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "assignors",
        indexes = {
                @Index(name = "idx_assignors_document", columnList = "document"),
                @Index(name = "idx_assignors_name", columnList = "name")
        }
)
public class AssignorEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "document", length = 32)
    private String document;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;
}
