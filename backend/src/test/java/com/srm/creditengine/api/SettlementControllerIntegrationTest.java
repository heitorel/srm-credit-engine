package com.srm.creditengine.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.srm.creditengine.integration.AbstractMySqlIntegrationTest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ContextConfiguration(classes = SettlementControllerIntegrationTest.FixedClockConfiguration.class)
@EnabledIf("isDockerAvailable")
class SettlementControllerIntegrationTest extends AbstractMySqlIntegrationTest {

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
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    jdbcTemplate.update("DELETE FROM settlement_items");
    jdbcTemplate.update("DELETE FROM settlements");
    jdbcTemplate.update("DELETE FROM receivables");
    jdbcTemplate.update("DELETE FROM assignors");
    jdbcTemplate.update("DELETE FROM exchange_rates");
  }

  @Test
  void shouldCreateSameCurrencySettlementAndPersistAuditSnapshot() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/settlements")
                    .contentType(APPLICATION_JSON)
                    .content(validSameCurrencyRequest()))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", matchesPattern("/api/settlements/.+")))
            .andExpect(jsonPath("$.sourceCurrency").value("BRL"))
            .andExpect(jsonPath("$.paymentCurrency").value("BRL"))
            .andExpect(jsonPath("$.status").value("SETTLED"))
            .andExpect(jsonPath("$.itemCount").value(1))
            .andExpect(jsonPath("$.totalFaceValue").value(10000.00))
            .andExpect(jsonPath("$.totalPresentValue").value(9518.14))
            .andExpect(jsonPath("$.totalPaymentValue").value(9518.14))
            .andExpect(jsonPath("$.items[0].externalReference").value("NF-1001"))
            .andExpect(jsonPath("$.items[0].spread").value(0.01500000))
            .andExpect(jsonPath("$.items[0].exchangeRate").doesNotExist())
            .andReturn();

    String location = result.getResponse().getHeader("Location");
    assertThat(location).isNotNull();

    Integer settlementCount =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlements", Integer.class);
    Integer settlementItemCount =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlement_items", Integer.class);
    Integer settledReceivableCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM receivables WHERE status = 'SETTLED'", Integer.class);

    assertThat(settlementCount).isEqualTo(1);
    assertThat(settlementItemCount).isEqualTo(1);
    assertThat(settledReceivableCount).isEqualTo(1);

    Map<String, Object> settlementRow =
        jdbcTemplate.queryForMap(
            """
                SELECT source_currency_code, payment_currency_code, total_face_value, total_present_value, total_payment_value
                FROM settlements
                """);
    assertThat(settlementRow.get("source_currency_code")).isEqualTo("BRL");
    assertThat(settlementRow.get("payment_currency_code")).isEqualTo("BRL");
    assertThat(settlementRow.get("total_face_value").toString()).startsWith("10000");
    assertThat(settlementRow.get("total_present_value").toString()).startsWith("9518.14");
    assertThat(settlementRow.get("total_payment_value").toString()).startsWith("9518.14");

    Map<String, Object> itemRow =
        jdbcTemplate.queryForMap(
            """
                SELECT external_reference, base_rate, spread, payment_value, exchange_rate
                FROM settlement_items
                """);
    assertThat(itemRow.get("external_reference")).isEqualTo("NF-1001");
    assertThat(itemRow.get("base_rate").toString()).startsWith("0.01000000");
    assertThat(itemRow.get("spread").toString()).startsWith("0.01500000");
    assertThat(itemRow.get("payment_value").toString()).startsWith("9518.14");
    assertThat(itemRow.get("exchange_rate")).isNull();
  }

  @Test
  void shouldCreateCrossCurrencySettlement() throws Exception {
    insertExchangeRate("66666666-6666-6666-6666-666666666666", "BRL", "USD", "0.19000000");

    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(validCrossCurrencyRequest()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.paymentCurrency").value("USD"))
        .andExpect(jsonPath("$.totalPresentValue").value(9518.14))
        .andExpect(jsonPath("$.totalPaymentValue").value(1808.45))
        .andExpect(jsonPath("$.items[0].paymentValue").value(1808.45))
        .andExpect(jsonPath("$.items[0].exchangeRate").value(0.19000000));
  }

  @Test
  void shouldRejectEmptySettlementBatch() throws Exception {
    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                {
                                  "assignor": {
                                    "name": "ACME Comercio Ltda.",
                                    "document": "12345678000199"
                                  },
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivables": []
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details[0].field").value("receivables"));
  }

  @Test
  void shouldRejectSettlementBatchAboveMaximumSize() throws Exception {
    StringBuilder receivablesJson = new StringBuilder();
    for (int index = 1; index <= 101; index++) {
      if (index > 1) {
        receivablesJson.append(",");
      }
      receivablesJson.append(
          """
                    {
                      "externalReference": "NF-%d",
                      "faceValue": 1000.00,
                      "sourceCurrency": "BRL",
                      "receivableType": "MERCANTILE_DUPLICATE",
                      "dueDate": "2026-09-05"
                    }
                    """
              .formatted(index));
    }

    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                {
                                  "assignor": {
                                    "name": "ACME Comercio Ltda.",
                                    "document": "12345678000199"
                                  },
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivables": [%s]
                                }
                                """
                        .formatted(receivablesJson)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details[0].field").value("receivables"));
  }

  @Test
  void shouldRejectMixedSourceCurrencyBatchWithStructuredError() throws Exception {
    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                {
                                  "assignor": {
                                    "name": "ACME Comercio Ltda.",
                                    "document": "12345678000199"
                                  },
                                  "paymentCurrency": "USD",
                                  "baseRate": 0.01000000,
                                  "receivables": [
                                    {
                                      "externalReference": "NF-1001",
                                      "faceValue": 10000.00,
                                      "sourceCurrency": "BRL",
                                      "receivableType": "MERCANTILE_DUPLICATE",
                                      "dueDate": "2026-09-05"
                                    },
                                    {
                                      "externalReference": "NF-1002",
                                      "faceValue": 5000.00,
                                      "sourceCurrency": "USD",
                                      "receivableType": "POST_DATED_CHECK",
                                      "dueDate": "2026-10-15"
                                    }
                                  ]
                                }
                                """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(
            jsonPath("$.message")
                .value(
                    "All receivables in the settlement batch must share the same source currency."))
        .andExpect(jsonPath("$.details[0].field").value("receivables"));
  }

  @Test
  void shouldRejectDuplicateReceivableInsideSameRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                {
                                  "assignor": {
                                    "name": "ACME Comercio Ltda.",
                                    "document": "12345678000199"
                                  },
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivables": [
                                    {
                                      "externalReference": "NF-1001",
                                      "faceValue": 10000.00,
                                      "sourceCurrency": "BRL",
                                      "receivableType": "MERCANTILE_DUPLICATE",
                                      "dueDate": "2026-09-05"
                                    },
                                    {
                                      "externalReference": "NF-1001",
                                      "faceValue": 5000.00,
                                      "sourceCurrency": "BRL",
                                      "receivableType": "POST_DATED_CHECK",
                                      "dueDate": "2026-10-15"
                                    }
                                  ]
                                }
                                """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.details[0].field").value("receivables[1].externalReference"));
  }

  @Test
  void shouldRollbackEntireSettlementWhenAnyItemFails() throws Exception {
    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                                {
                                  "assignor": {
                                    "name": "ACME Comercio Ltda.",
                                    "document": "12345678000199"
                                  },
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivables": [
                                    {
                                      "externalReference": "NF-1001",
                                      "faceValue": 10000.00,
                                      "sourceCurrency": "BRL",
                                      "receivableType": "MERCANTILE_DUPLICATE",
                                      "dueDate": "2026-09-05"
                                    },
                                    {
                                      "externalReference": "NF-1002",
                                      "faceValue": 5000.00,
                                      "sourceCurrency": "BRL",
                                      "receivableType": "POST_DATED_CHECK",
                                      "dueDate": "2026-07-06"
                                    }
                                  ]
                                }
                                """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.details[0].field").value("dueDate"));

    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM assignors", Integer.class))
        .isZero();
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM receivables", Integer.class))
        .isZero();
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlements", Integer.class))
        .isZero();
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlement_items", Integer.class))
        .isZero();
  }

  @Test
  void shouldPersistNothingWhenExchangeRateIsMissing() throws Exception {
    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(validCrossCurrencyRequest()))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(
            jsonPath("$.message").value("Missing exchange rate for currency pair BRL -> USD."));

    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM assignors", Integer.class))
        .isZero();
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM receivables", Integer.class))
        .isZero();
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlements", Integer.class))
        .isZero();
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlement_items", Integer.class))
        .isZero();
  }

  @Test
  void shouldRejectAlreadySettledReceivableAndReturnConflictForDuplicateSettlement()
      throws Exception {
    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(validSameCurrencyRequest()))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(validSameCurrencyRequest()))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message")
                .value("Receivable NF-1001 for assignor 12345678000199 has already been settled."));
  }

  @Test
  void shouldReturnSettlementDetailWithPersistedSnapshotsAndNotRecalculateHistoricalValues()
      throws Exception {
    insertExchangeRate("66666666-6666-6666-6666-666666666666", "BRL", "USD", "0.19000000");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/settlements")
                    .contentType(APPLICATION_JSON)
                    .content(validCrossCurrencyRequest()))
            .andExpect(status().isCreated())
            .andReturn();

    String location = createResult.getResponse().getHeader("Location");
    assertThat(location).isNotNull();

    insertExchangeRate("77777777-7777-7777-7777-777777777777", "BRL", "USD", "0.20000000");

    mockMvc
        .perform(get(location))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[0].exchangeRate").value(0.19000000))
        .andExpect(jsonPath("$.items[0].paymentValue").value(1808.45))
        .andExpect(jsonPath("$.totalPaymentValue").value(1808.45));
  }

  @Test
  void shouldReturnNotFoundForUnknownSettlement() throws Exception {
    mockMvc
        .perform(get("/api/settlements/11111111-1111-1111-1111-111111111111"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Settlement not found."));
  }

  @Test
  void shouldPreventDuplicateSettlementAtDatabaseConstraintLevel() throws Exception {
    mockMvc
        .perform(
            post("/api/settlements")
                .contentType(APPLICATION_JSON)
                .content(validSameCurrencyRequest()))
        .andExpect(status().isCreated());

    Map<String, Object> baseSettlement =
        jdbcTemplate.queryForMap(
            """
                SELECT id, assignor_id, source_currency_code, payment_currency_code, base_rate
                FROM settlements
                """);
    Map<String, Object> firstItem =
        jdbcTemplate.queryForMap(
            """
                SELECT receivable_id, receivable_type_code, face_value, source_currency_code, payment_currency_code,
                       base_rate, spread, term_in_months, present_value_source, discount_value, payment_value
                FROM settlement_items
                """);

    jdbcTemplate.update(
        """
                INSERT INTO settlements (
                    id, assignor_id, source_currency_code, payment_currency_code, status, base_rate,
                    item_count, total_face_value, total_present_value, total_payment_value,
                    settled_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, 'SETTLED', ?, 1, 10000.00, 9518.14, 9518.14, ?, ?, ?)
                """,
        "88888888-8888-8888-8888-888888888888",
        baseSettlement.get("assignor_id"),
        baseSettlement.get("source_currency_code"),
        baseSettlement.get("payment_currency_code"),
        baseSettlement.get("base_rate"),
        java.sql.Timestamp.from(Instant.parse("2026-07-07T13:30:00Z")),
        java.sql.Timestamp.from(Instant.parse("2026-07-07T13:30:00Z")),
        java.sql.Timestamp.from(Instant.parse("2026-07-07T13:30:00Z")));

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    """
                        INSERT INTO settlement_items (
                            id, settlement_id, receivable_id, external_reference, receivable_type_code,
                            face_value, source_currency_code, payment_currency_code, base_rate, spread,
                            term_in_months, present_value_source, discount_value, payment_value,
                            exchange_rate, calculated_at, created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                    "99999999-9999-9999-9999-999999999999",
                    "88888888-8888-8888-8888-888888888888",
                    firstItem.get("receivable_id"),
                    "NF-1001-DUP",
                    firstItem.get("receivable_type_code"),
                    firstItem.get("face_value"),
                    firstItem.get("source_currency_code"),
                    firstItem.get("payment_currency_code"),
                    firstItem.get("base_rate"),
                    firstItem.get("spread"),
                    firstItem.get("term_in_months"),
                    firstItem.get("present_value_source"),
                    firstItem.get("discount_value"),
                    firstItem.get("payment_value"),
                    null,
                    java.sql.Timestamp.from(Instant.parse("2026-07-07T13:30:00Z")),
                    java.sql.Timestamp.from(Instant.parse("2026-07-07T13:30:00Z"))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  private void insertExchangeRate(
      String id, String sourceCurrency, String targetCurrency, String rate) {
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
        java.sql.Timestamp.from(Instant.parse("2026-07-07T13:00:00Z")),
        java.sql.Timestamp.from(Instant.parse("2026-07-07T13:05:00Z")));
  }

  private String validSameCurrencyRequest() {
    return """
                {
                  "assignor": {
                    "name": "ACME Comercio Ltda.",
                    "document": "12345678000199"
                  },
                  "paymentCurrency": "BRL",
                  "baseRate": 0.01000000,
                  "receivables": [
                    {
                      "externalReference": "NF-1001",
                      "faceValue": 10000.00,
                      "sourceCurrency": "BRL",
                      "receivableType": "MERCANTILE_DUPLICATE",
                      "dueDate": "2026-09-05"
                    }
                  ]
                }
                """;
  }

  private String validCrossCurrencyRequest() {
    return """
                {
                  "assignor": {
                    "name": "ACME Comercio Ltda.",
                    "document": "12345678000199"
                  },
                  "paymentCurrency": "USD",
                  "baseRate": 0.01000000,
                  "receivables": [
                    {
                      "externalReference": "NF-1001",
                      "faceValue": 10000.00,
                      "sourceCurrency": "BRL",
                      "receivableType": "MERCANTILE_DUPLICATE",
                      "dueDate": "2026-09-05"
                    }
                  ]
                }
                """;
  }

  @TestConfiguration
  static class FixedClockConfiguration {

    @Bean
    @Primary
    Clock fixedClock() {
      return Clock.fixed(Instant.parse("2026-07-07T13:30:00Z"), ZoneOffset.UTC);
    }
  }
}
