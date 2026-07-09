package com.srm.creditengine.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.srm.creditengine.domain.shared.Term;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TermTest {

  private final Clock fixedClock =
      Clock.fixed(Instant.parse("2026-07-07T13:20:00Z"), ZoneOffset.UTC);

  @Test
  void shouldCalculateCommercialMonthTermFromDueDate() {
    LocalDate pricingDate = LocalDate.now(fixedClock);

    assertThat(Term.between(pricingDate, pricingDate.plusDays(60)).value())
        .isEqualByComparingTo("2.00000000");
  }

  @Test
  void shouldRejectPastDueDate() {
    LocalDate pricingDate = LocalDate.now(fixedClock);

    assertThatThrownBy(() -> Term.between(pricingDate, pricingDate.minusDays(1)))
        .isInstanceOf(InvalidDueDateException.class);
  }

  @Test
  void shouldRejectSameDayDueDate() {
    LocalDate pricingDate = LocalDate.now(fixedClock);

    assertThatThrownBy(() -> Term.between(pricingDate, pricingDate))
        .isInstanceOf(InvalidDueDateException.class);
  }

  @Test
  void shouldUseFixedClockForDeterministicTermCalculation() {
    LocalDate pricingDate = LocalDate.now(fixedClock);

    assertThat(pricingDate).isEqualTo(LocalDate.of(2026, 7, 7));
    assertThat(Term.between(pricingDate, LocalDate.of(2026, 9, 7)).value())
        .isEqualByComparingTo("2.06666667");
  }
}
