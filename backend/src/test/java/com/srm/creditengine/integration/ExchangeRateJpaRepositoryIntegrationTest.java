package com.srm.creditengine.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.repository.ExchangeRateJpaRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EnabledIf("isDockerAvailable")
class ExchangeRateJpaRepositoryIntegrationTest extends AbstractMySqlIntegrationTest {

  @Autowired private ExchangeRateJpaRepository repository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
  }

  @BeforeEach
  void cleanExchangeRates() {
    jdbcTemplate.update("DELETE FROM exchange_rates");
  }

  @Test
  void shouldFindLatestExchangeRateByExactPair() {
    insertExchangeRate(
        "9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01",
        "USD",
        "BRL",
        "5.25000000",
        "2026-07-07T13:00:00Z",
        "2026-07-07T13:05:00Z");

    Optional<ExchangeRateEntity> result =
        repository
            .findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(
                "USD", "BRL");

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getId()).isEqualTo("9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01");
  }

  @Test
  void shouldReturnMostRecentValidExchangeRate() {
    insertExchangeRate(
        "11111111-1111-1111-1111-111111111111",
        "USD",
        "BRL",
        "5.10000000",
        "2026-07-06T13:00:00Z",
        "2026-07-07T13:05:00Z");
    insertExchangeRate(
        "22222222-2222-2222-2222-222222222222",
        "USD",
        "BRL",
        "5.30000000",
        "2026-07-08T13:00:00Z",
        "2026-07-07T13:04:00Z");

    Optional<ExchangeRateEntity> result =
        repository
            .findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(
                "USD", "BRL");

    assertThat(result).isPresent();
    assertThat(result.orElseThrow().getId()).isEqualTo("22222222-2222-2222-2222-222222222222");
    assertThat(result.orElseThrow().getRate()).isEqualByComparingTo("5.30000000");
  }

  @Test
  void shouldNotFindInverseExchangeRateWhenOnlyOppositePairExists() {
    insertExchangeRate(
        "55555555-5555-5555-5555-555555555555",
        "USD",
        "BRL",
        "5.25000000",
        "2026-07-07T13:00:00Z",
        "2026-07-07T13:05:00Z");

    Optional<ExchangeRateEntity> result =
        repository
            .findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(
                "BRL", "USD");

    assertThat(result).isEmpty();
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
