package com.srm.creditengine.domain.exchange;

import static org.assertj.core.api.Assertions.assertThat;

import com.srm.creditengine.api.request.CreateExchangeRateRequest;
import com.srm.creditengine.api.response.ExchangeRateResponse;
import com.srm.creditengine.domain.shared.Rate;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ExchangeRateValueTypesTest {

  @Test
  void shouldUseBigDecimalForExchangeRateValueAcrossLayers() throws Exception {
    assertThat(CreateExchangeRateRequest.class.getRecordComponents()[2].getType())
        .isEqualTo(BigDecimal.class);
    assertThat(ExchangeRateResponse.class.getRecordComponents()[3].getType())
        .isEqualTo(BigDecimal.class);
    assertThat(Rate.class.getRecordComponents()[0].getType()).isEqualTo(BigDecimal.class);
    assertThat(ExchangeRateEntity.class.getDeclaredField("rate").getType())
        .isEqualTo(BigDecimal.class);
  }
}
