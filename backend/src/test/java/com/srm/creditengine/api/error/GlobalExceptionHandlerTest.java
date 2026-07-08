package com.srm.creditengine.api.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.srm.creditengine.domain.shared.UnprocessableEntityException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    @Test
    void shouldBuildStructuredBusinessErrorResponse() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-07-08T12:00:00Z"), ZoneOffset.UTC);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(fixedClock);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/pricing/simulations");
        UnprocessableEntityException exception = new UnprocessableEntityException(
                "Missing exchange rate for currency pair BRL -> USD.",
                List.of(new ApiErrorDetail("paymentCurrency", "Exchange rate BRL -> USD is required."))
        );

        ApiErrorResponse response = handler.handleBusinessException(exception, request).getBody();

        assertThat(response).isNotNull();
        assertThat(response.timestamp()).isEqualTo(Instant.parse("2026-07-08T12:00:00Z"));
        assertThat(response.status()).isEqualTo(422);
        assertThat(response.error()).isEqualTo("Unprocessable Entity");
        assertThat(response.message()).isEqualTo("Missing exchange rate for currency pair BRL -> USD.");
        assertThat(response.path()).isEqualTo("/api/pricing/simulations");
        assertThat(response.details()).containsExactly(new ApiErrorDetail(
                "paymentCurrency",
                "Exchange rate BRL -> USD is required."
        ));
    }
}
