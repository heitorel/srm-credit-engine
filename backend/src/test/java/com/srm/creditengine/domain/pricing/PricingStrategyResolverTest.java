package com.srm.creditengine.domain.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PricingStrategyResolverTest {

  private final MercantileDuplicatePricingStrategy mercantileDuplicatePricingStrategy =
      new MercantileDuplicatePricingStrategy();
  private final PostDatedCheckPricingStrategy postDatedCheckPricingStrategy =
      new PostDatedCheckPricingStrategy();
  private final PricingStrategyResolver resolver =
      new PricingStrategyResolver(
          java.util.List.of(mercantileDuplicatePricingStrategy, postDatedCheckPricingStrategy));

  @Test
  void shouldReturnMercantileDuplicateSpread() {
    assertThat(mercantileDuplicatePricingStrategy.spread().value())
        .isEqualByComparingTo("0.01500000");
  }

  @Test
  void shouldReturnPostDatedCheckSpread() {
    assertThat(postDatedCheckPricingStrategy.spread().value()).isEqualByComparingTo("0.02500000");
  }

  @Test
  void shouldResolveMercantileDuplicateStrategy() {
    assertThat(resolver.resolve("MERCANTILE_DUPLICATE"))
        .isInstanceOf(MercantileDuplicatePricingStrategy.class);
  }

  @Test
  void shouldResolvePostDatedCheckStrategy() {
    assertThat(resolver.resolve("POST_DATED_CHECK"))
        .isInstanceOf(PostDatedCheckPricingStrategy.class);
  }

  @Test
  void shouldFailForUnsupportedReceivableType() {
    assertThatThrownBy(() -> resolver.resolve("INVOICE"))
        .isInstanceOf(UnsupportedReceivableTypeException.class)
        .hasMessage("Unsupported receivable type: INVOICE.");
  }
}
