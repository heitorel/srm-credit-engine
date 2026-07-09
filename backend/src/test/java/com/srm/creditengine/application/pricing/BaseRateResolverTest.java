package com.srm.creditengine.application.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BaseRateResolverTest {

  @Test
  void shouldUseRequestBaseRateWhenProvided() {
    BaseRateResolver resolver = new BaseRateResolver(new BigDecimal("0.02000000"));

    assertThat(resolver.resolve(new BigDecimal("0.01000000")).value())
        .isEqualByComparingTo("0.01000000");
  }

  @Test
  void shouldUseConfiguredDefaultBaseRateWhenRequestBaseRateIsOmitted() {
    BaseRateResolver resolver = new BaseRateResolver(new BigDecimal("0.02000000"));

    assertThat(resolver.resolve(null).value()).isEqualByComparingTo("0.02000000");
  }

  @Test
  void shouldFailWhenBaseRateIsMissingAndNoConfiguredDefaultExists() {
    BaseRateResolver resolver = new BaseRateResolver((BigDecimal) null);

    assertThatThrownBy(() -> resolver.resolve(null)).hasMessage("Base rate is required.");
  }
}
