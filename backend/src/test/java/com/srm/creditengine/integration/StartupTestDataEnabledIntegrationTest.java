package com.srm.creditengine.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.srm.creditengine.application.startup.StartupTestDataService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
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

@EnabledIf("isDockerAvailable")
@SpringBootTest(properties = "application.startup-test-data.enabled=true")
@ContextConfiguration(classes = StartupTestDataEnabledIntegrationTest.FixedClockConfiguration.class)
class StartupTestDataEnabledIntegrationTest extends AbstractMySqlIntegrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private StartupTestDataService startupTestDataService;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
  }

  @Test
  void shouldSeedVisibleStartupDatasetForLocalValidation() {
    assertThat(count("assignors")).isEqualTo(4);
    assertThat(count("exchange_rates")).isEqualTo(4);
    assertThat(count("receivables")).isEqualTo(7);
    assertThat(count("settlements")).isEqualTo(3);
    assertThat(count("settlement_items")).isEqualTo(5);
    assertThat(countByStatus("receivables", "AVAILABLE")).isEqualTo(2);
    assertThat(countByStatus("receivables", "SETTLED")).isEqualTo(5);

    BigDecimal latestBrlUsd =
        jdbcTemplate.queryForObject(
            """
                SELECT rate
                FROM exchange_rates
                WHERE source_currency_code = 'BRL'
                  AND target_currency_code = 'USD'
                ORDER BY valid_at DESC, created_at DESC, id DESC
                LIMIT 1
                """,
            BigDecimal.class);
    assertThat(latestBrlUsd).isEqualByComparingTo("0.19000000");

    Map<String, Object> statementShape =
        jdbcTemplate.queryForMap(
            """
                SELECT
                    SUM(CASE WHEN source_currency_code = payment_currency_code THEN 1 ELSE 0 END) AS same_currency,
                    SUM(CASE WHEN source_currency_code <> payment_currency_code THEN 1 ELSE 0 END) AS cross_currency
                FROM settlements
                """);
    assertThat(((Number) statementShape.get("same_currency")).intValue()).isEqualTo(1);
    assertThat(((Number) statementShape.get("cross_currency")).intValue()).isEqualTo(2);
  }

  @Test
  void shouldRemainIdempotentWhenSeedRunsAgain() {
    Map<String, Integer> countsBefore = snapshotCounts();

    startupTestDataService.seed();

    assertThat(snapshotCounts()).isEqualTo(countsBefore);
  }

  private Map<String, Integer> snapshotCounts() {
    return Map.of(
        "assignors", count("assignors"),
        "exchange_rates", count("exchange_rates"),
        "receivables", count("receivables"),
        "settlements", count("settlements"),
        "settlement_items", count("settlement_items"));
  }

  private int count(String tableName) {
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
  }

  private int countByStatus(String tableName, String status) {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM " + tableName + " WHERE status = ?", Integer.class, status);
  }

  @TestConfiguration
  static class FixedClockConfiguration {

    @Bean
    @Primary
    Clock fixedClock() {
      return Clock.fixed(Instant.parse("2026-07-09T10:00:00Z"), ZoneOffset.UTC);
    }
  }
}
