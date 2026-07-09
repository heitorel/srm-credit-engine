package com.srm.creditengine.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "currencies")
public class CurrencyEntity {

  @Id
  @Column(name = "code", nullable = false, length = 3, columnDefinition = "CHAR(3)")
  private String code;

  @Column(name = "name", nullable = false, length = 64)
  private String name;

  @Column(name = "decimal_places", nullable = false, columnDefinition = "TINYINT")
  private Integer decimalPlaces;

  @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(6)")
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
  private LocalDateTime updatedAt;

  protected CurrencyEntity() {}

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public Integer getDecimalPlaces() {
    return decimalPlaces;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
