package com.srm.creditengine.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@EnabledIf("isDockerAvailable")
@SpringBootTest(properties = "application.startup-test-data.enabled=false")
class StartupTestDataDisabledIntegrationTest extends AbstractMySqlIntegrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
  }

  @Test
  void shouldNotSeedNonReferenceDataWhenStartupDatasetIsDisabled() {
    List<String> currencies =
        jdbcTemplate.queryForList("SELECT code FROM currencies ORDER BY code", String.class);

    assertThat(currencies).containsExactly("BRL", "USD");
    assertThat(count("assignors")).isZero();
    assertThat(count("exchange_rates")).isZero();
    assertThat(count("receivables")).isZero();
    assertThat(count("settlements")).isZero();
    assertThat(count("settlement_items")).isZero();
  }

  private int count(String tableName) {
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
  }
}
