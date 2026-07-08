package com.srm.creditengine.api;

import com.srm.creditengine.integration.AbstractMySqlIntegrationTest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = PricingSimulationControllerIntegrationTest.FixedClockConfiguration.class)
@EnabledIf("isDockerAvailable")
class PricingSimulationControllerIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        jdbcTemplate.update("DELETE FROM exchange_rates");
        jdbcTemplate.update("DELETE FROM settlement_items");
        jdbcTemplate.update("DELETE FROM settlements");
    }

    @Test
    void shouldSimulateSameCurrencyPricing() throws Exception {
        mockMvc.perform(post("/api/pricing/simulations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 10000.00,
                                  "sourceCurrency": "BRL",
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivableType": "MERCANTILE_DUPLICATE",
                                  "dueDate": "2026-09-05"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presentValueInSourceCurrency").value(9518.14))
                .andExpect(jsonPath("$.netPaymentValue").value(9518.14))
                .andExpect(jsonPath("$.discountValue").value(481.86))
                .andExpect(jsonPath("$.exchangeRate").doesNotExist());
    }

    @Test
    void shouldSimulateCrossCurrencyPricing() throws Exception {
        insertExchangeRate("66666666-6666-6666-6666-666666666666", "BRL", "USD", "0.19000000");

        mockMvc.perform(post("/api/pricing/simulations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 10000.00,
                                  "sourceCurrency": "BRL",
                                  "paymentCurrency": "USD",
                                  "baseRate": 0.01000000,
                                  "receivableType": "MERCANTILE_DUPLICATE",
                                  "dueDate": "2026-09-05"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presentValueInSourceCurrency").value(9518.14))
                .andExpect(jsonPath("$.netPaymentValue").value(1808.45))
                .andExpect(jsonPath("$.exchangeRate").value(0.19000000));
    }

    @Test
    void shouldRejectInvalidFaceValue() throws Exception {
        mockMvc.perform(post("/api/pricing/simulations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 0,
                                  "sourceCurrency": "BRL",
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivableType": "MERCANTILE_DUPLICATE",
                                  "dueDate": "2026-09-05"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("faceValue"));
    }

    @Test
    void shouldRejectPastDueDate() throws Exception {
        mockMvc.perform(post("/api/pricing/simulations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 10000.00,
                                  "sourceCurrency": "BRL",
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivableType": "MERCANTILE_DUPLICATE",
                                  "dueDate": "2026-07-06"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details[0].field").value("dueDate"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenExchangeRateIsMissing() throws Exception {
        mockMvc.perform(post("/api/pricing/simulations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 10000.00,
                                  "sourceCurrency": "BRL",
                                  "paymentCurrency": "USD",
                                  "baseRate": 0.01000000,
                                  "receivableType": "MERCANTILE_DUPLICATE",
                                  "dueDate": "2026-09-05"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Missing exchange rate for currency pair BRL -> USD."));
    }

    @Test
    void shouldReturnUnprocessableEntityForUnsupportedReceivableType() throws Exception {
        mockMvc.perform(post("/api/pricing/simulations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 10000.00,
                                  "sourceCurrency": "BRL",
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivableType": "INVOICE",
                                  "dueDate": "2026-09-05"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details[0].field").value("receivableType"));
    }

    @Test
    void shouldNotPersistSettlementRecordsDuringSimulation() throws Exception {
        mockMvc.perform(post("/api/pricing/simulations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "faceValue": 10000.00,
                                  "sourceCurrency": "BRL",
                                  "paymentCurrency": "BRL",
                                  "baseRate": 0.01000000,
                                  "receivableType": "MERCANTILE_DUPLICATE",
                                  "dueDate": "2026-09-05"
                                }
                                """))
                .andExpect(status().isOk());

        Integer settlementCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlements", Integer.class);
        Integer settlementItemCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlement_items", Integer.class);

        org.assertj.core.api.Assertions.assertThat(settlementCount).isZero();
        org.assertj.core.api.Assertions.assertThat(settlementItemCount).isZero();
    }

    private void insertExchangeRate(String id, String sourceCurrency, String targetCurrency, String rate) {
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
                java.sql.Timestamp.from(Instant.parse("2026-07-07T13:05:00Z"))
        );
    }

    @TestConfiguration
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-07-07T13:20:00Z"), ZoneOffset.UTC);
        }
    }
}
