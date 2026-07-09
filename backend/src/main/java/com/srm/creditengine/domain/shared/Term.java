package com.srm.creditengine.domain.shared;

import com.srm.creditengine.domain.pricing.InvalidDueDateException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Term(BigDecimal value) {

  public Term {
    if (value == null) {
      throw new IllegalArgumentException("Term is required.");
    }
    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Term must be greater than zero.");
    }
  }

  public static Term between(LocalDate pricingDate, LocalDate dueDate) {
    long daysBetween = ChronoUnit.DAYS.between(pricingDate, dueDate);
    if (daysBetween <= 0) {
      throw new InvalidDueDateException();
    }

    BigDecimal months =
        BigDecimal.valueOf(daysBetween)
            .divide(new BigDecimal("30"), FinancialMath.RATE_SCALE, java.math.RoundingMode.HALF_UP);

    return new Term(months);
  }
}
