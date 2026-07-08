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

    protected AssignorEntity() {
    }

    private AssignorEntity(
            String id,
            String name,
            String document,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.document = document;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AssignorEntity create(
            String id,
            String name,
            String document,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new AssignorEntity(id, name, document, createdAt, updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
