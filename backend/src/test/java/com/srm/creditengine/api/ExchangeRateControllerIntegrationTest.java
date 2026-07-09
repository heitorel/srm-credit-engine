package com.srm.creditengine.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.srm.creditengine.integration.AbstractMySqlIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@EnabledIf("isDockerAvailable")
class ExchangeRateControllerIntegrationTest extends AbstractMySqlIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private JdbcTemplate jdbcTemplate;

  private MockMvc mockMvc;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
  }

  @BeforeEach
  void cleanExchangeRates() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    jdbcTemplate.update("DELETE FROM exchange_rates");
  }

  @Test
  void shouldCreateExchangeRate() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "BRL",
                                  "rate": 5.25000000,
                                  "validAt": "2026-07-07T13:00:00Z"
                                }
                                """))
        .andExpect(status().isCreated())
        .andExpect(
            header()
                .string("Location", org.hamcrest.Matchers.matchesPattern("/api/exchange-rates/.+")))
        .andExpect(jsonPath("$.sourceCurrency").value("USD"))
        .andExpect(jsonPath("$.targetCurrency").value("BRL"))
        .andExpect(jsonPath("$.rate").value(5.25000000))
        .andExpect(jsonPath("$.validAt").value("2026-07-07T13:00:00Z"))
        .andExpect(jsonPath("$.createdAt").exists());
  }

  @Test
  void shouldRejectExchangeRateWithSameSourceAndTarget() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "BRL",
                                  "targetCurrency": "BRL",
                                  "rate": 1.00000000,
                                  "validAt": "2026-07-07T13:00:00Z"
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value("Source currency and target currency must be different."))
        .andExpect(jsonPath("$.details[0].field").value("targetCurrency"));
  }

  @Test
  void shouldRejectExchangeRateWithUnsupportedSourceCurrency() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "EUR",
                                  "targetCurrency": "BRL",
                                  "rate": 5.25000000,
                                  "validAt": "2026-07-07T13:00:00Z"
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Unsupported currency: EUR."))
        .andExpect(jsonPath("$.details[0].field").value("sourceCurrency"));
  }

  @Test
  void shouldRejectExchangeRateWithUnsupportedTargetCurrency() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "EUR",
                                  "rate": 5.25000000,
                                  "validAt": "2026-07-07T13:00:00Z"
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Unsupported currency: EUR."))
        .andExpect(jsonPath("$.details[0].field").value("targetCurrency"));
  }

  @Test
  void shouldRejectExchangeRateWithZeroRate() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "BRL",
                                  "rate": 0,
                                  "validAt": "2026-07-07T13:00:00Z"
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details[0].field").value("rate"));
  }

  @Test
  void shouldRejectExchangeRateWithNegativeRate() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "BRL",
                                  "rate": -5.25,
                                  "validAt": "2026-07-07T13:00:00Z"
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details[0].field").value("rate"));
  }

  @Test
  void shouldRejectExchangeRateWithoutValidAt() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "BRL",
                                  "rate": 5.25000000
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details[0].field").value("validAt"));
  }

  @Test
  void shouldFindLatestExchangeRateByExactPair() throws Exception {
    String rateId = "9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01";
    insertExchangeRate(
        rateId, "USD", "BRL", "5.25000000", "2026-07-07T13:00:00Z", "2026-07-07T13:05:00Z");

    mockMvc
        .perform(
            get("/api/exchange-rates/latest")
                .queryParam("sourceCurrency", "USD")
                .queryParam("targetCurrency", "BRL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(rateId))
        .andExpect(jsonPath("$.sourceCurrency").value("USD"))
        .andExpect(jsonPath("$.targetCurrency").value("BRL"))
        .andExpect(jsonPath("$.rate").value(5.25000000));
  }

  @Test
  void shouldReturnMostRecentValidExchangeRate() throws Exception {
    insertExchangeRate(
        "11111111-1111-1111-1111-111111111111",
        "USD",
        "BRL",
        "5.10000000",
        "2026-07-07T13:00:00Z",
        "2026-07-07T13:05:00Z");
    insertExchangeRate(
        "22222222-2222-2222-2222-222222222222",
        "USD",
        "BRL",
        "5.25000000",
        "2026-07-08T13:00:00Z",
        "2026-07-07T13:06:00Z");

    mockMvc
        .perform(
            get("/api/exchange-rates/latest")
                .queryParam("sourceCurrency", "USD")
                .queryParam("targetCurrency", "BRL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("22222222-2222-2222-2222-222222222222"))
        .andExpect(jsonPath("$.rate").value(5.25000000));
  }

  @Test
  void shouldUseCreatedAtAsTieBreakerWhenValidAtIsEqual() throws Exception {
    insertExchangeRate(
        "33333333-3333-3333-3333-333333333333",
        "USD",
        "BRL",
        "5.20000000",
        "2026-07-07T13:00:00Z",
        "2026-07-07T13:05:00Z");
    insertExchangeRate(
        "44444444-4444-4444-4444-444444444444",
        "USD",
        "BRL",
        "5.30000000",
        "2026-07-07T13:00:00Z",
        "2026-07-07T13:06:00Z");

    mockMvc
        .perform(
            get("/api/exchange-rates/latest")
                .queryParam("sourceCurrency", "USD")
                .queryParam("targetCurrency", "BRL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("44444444-4444-4444-4444-444444444444"))
        .andExpect(jsonPath("$.rate").value(5.30000000));
  }

  @Test
  void shouldNotFindInverseExchangeRateWhenOnlyOppositePairExists() throws Exception {
    insertExchangeRate(
        "55555555-5555-5555-5555-555555555555",
        "USD",
        "BRL",
        "5.25000000",
        "2026-07-07T13:00:00Z",
        "2026-07-07T13:05:00Z");

    mockMvc
        .perform(
            get("/api/exchange-rates/latest")
                .queryParam("sourceCurrency", "BRL")
                .queryParam("targetCurrency", "USD"))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.message").value("No exchange rate found for currency pair BRL -> USD."));
  }

  @Test
  void shouldReturnNotFoundWhenLatestExchangeRateDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/api/exchange-rates/latest")
                .queryParam("sourceCurrency", "USD")
                .queryParam("targetCurrency", "BRL"))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.message").value("No exchange rate found for currency pair USD -> BRL."));
  }

  @Test
  void shouldReturnStructuredErrorForInvalidCurrencyPair() throws Exception {
    mockMvc
        .perform(
            get("/api/exchange-rates/latest")
                .queryParam("sourceCurrency", "USD")
                .queryParam("targetCurrency", "USD"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.path").value("/api/exchange-rates/latest"));
  }

  @Test
  void shouldReturnStructuredErrorForMissingExchangeRate() throws Exception {
    mockMvc
        .perform(
            get("/api/exchange-rates/latest")
                .queryParam("sourceCurrency", "USD")
                .queryParam("targetCurrency", "BRL"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.path").value("/api/exchange-rates/latest"))
        .andExpect(jsonPath("$.details").isArray());
  }

  @Test
  void shouldReturnStructuredValidationErrorForInvalidRate() throws Exception {
    mockMvc
        .perform(
            post("/api/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "sourceCurrency": "USD",
                                  "targetCurrency": "BRL",
                                  "rate": 0,
                                  "validAt": "2026-07-07T13:00:00Z"
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.path").value("/api/exchange-rates"))
        .andExpect(jsonPath("$.details[0].field").value("rate"));
  }

  private void insertExchangeRate(
      String id,
      String sourceCurrency,
      String targetCurrency,
      String rate,
      String validAt,
      String createdAt) {
    jdbcTemplate.update(
        """
                INSERT INTO exchange_rates (
                    id,
                    source_currency_code,
                    target_currency_code,
                    rate,
                    valid_at,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
        id,
        sourceCurrency,
        targetCurrency,
        rate,
        java.sql.Timestamp.from(Instant.parse(validAt)),
        java.sql.Timestamp.from(Instant.parse(createdAt)));
  }
}
