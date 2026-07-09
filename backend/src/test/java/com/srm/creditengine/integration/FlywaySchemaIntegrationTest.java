package com.srm.creditengine.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

@EnabledIf("isDockerAvailable")
@SpringBootTest
class FlywaySchemaIntegrationTest {

  static final MySQLContainer<?> MYSQL =
      new MySQLContainer<>("mysql:8.4.10")
          .withDatabaseName("srm_credit_engine_test")
          .withUsername("test")
          .withPassword("test");

  @BeforeAll
  static void startContainer() {
    MYSQL.start();
  }

  @AfterAll
  static void stopContainer() {
    MYSQL.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
  }

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private Environment environment;

  @Test
  void shouldStartWithFlywayMigrationsAndHibernateValidationEnabled() {
    assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    assertThat(tableNames())
        .containsExactlyInAnyOrder(
            "assignors",
            "currencies",
            "exchange_rates",
            "flyway_schema_history",
            "receivables",
            "receivable_types",
            "settlement_items",
            "settlements");
  }

  @Test
  void shouldSeedSupportedCurrenciesAndReceivableTypes() {
    List<String> currencies =
        jdbcTemplate.queryForList("SELECT code FROM currencies ORDER BY code", String.class);
    List<String> receivableTypes =
        jdbcTemplate.queryForList("SELECT code FROM receivable_types ORDER BY code", String.class);
    BigDecimal mercantileSpread =
        jdbcTemplate.queryForObject(
            "SELECT monthly_spread FROM receivable_types WHERE code = 'MERCANTILE_DUPLICATE'",
            BigDecimal.class);
    BigDecimal postDatedSpread =
        jdbcTemplate.queryForObject(
            "SELECT monthly_spread FROM receivable_types WHERE code = 'POST_DATED_CHECK'",
            BigDecimal.class);

    assertThat(currencies).containsExactly("BRL", "USD");
    assertThat(receivableTypes).containsExactly("MERCANTILE_DUPLICATE", "POST_DATED_CHECK");
    assertThat(mercantileSpread).isEqualByComparingTo("0.01500000");
    assertThat(postDatedSpread).isEqualByComparingTo("0.02500000");
  }

  @Test
  void shouldUseDecimalColumnsForFinancialData() {
    Integer floatingPointColumns =
        jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name IN ('exchange_rates', 'receivables', 'settlements', 'settlement_items', 'receivable_types')
                  AND column_name IN (
                      'monthly_spread',
                      'rate',
                      'face_value',
                      'base_rate',
                      'total_face_value',
                      'total_present_value',
                      'total_payment_value',
                      'spread',
                      'term_in_months',
                      'present_value_source',
                      'discount_value',
                      'payment_value',
                      'exchange_rate'
                  )
                  AND data_type IN ('float', 'double', 'real')
                """,
            Integer.class);

    assertThat(floatingPointColumns).isZero();
    assertThat(columnType("receivables", "face_value")).isEqualTo("decimal(19,4)");
    assertThat(columnType("receivable_types", "monthly_spread")).isEqualTo("decimal(19,8)");
    assertThat(columnType("exchange_rates", "rate")).isEqualTo("decimal(19,8)");
    assertThat(columnType("settlements", "total_payment_value")).isEqualTo("decimal(19,4)");
    assertThat(columnType("settlement_items", "term_in_months")).isEqualTo("decimal(19,8)");
  }

  @Test
  void shouldCreateCriticalConstraintsAndIndexesForIntegrityAndReporting() {
    String receivablesDdl = showCreateTable("receivables");
    String exchangeRatesDdl = showCreateTable("exchange_rates");
    String settlementsDdl = showCreateTable("settlements");
    String settlementItemsDdl = showCreateTable("settlement_items");

    assertThat(receivablesDdl)
        .contains(
            "UNIQUE KEY `uk_receivables_assignor_external_reference` (`assignor_id`,`external_reference`)");
    assertThat(settlementItemsDdl)
        .contains("UNIQUE KEY `uk_settlement_items_receivable` (`receivable_id`)");
    assertThat(exchangeRatesDdl)
        .contains(
            "KEY `idx_exchange_rates_pair_valid_at` (`source_currency_code`,`target_currency_code`,`valid_at` DESC,`created_at` DESC)");
    assertThat(settlementsDdl)
        .contains("KEY `idx_settlements_statement_default` (`settled_at` DESC,`id`)");
    assertThat(settlementItemsDdl)
        .contains("KEY `idx_settlement_items_receivable_type` (`receivable_type_code`)");
    assertThat(settlementItemsDdl)
        .contains("KEY `idx_settlement_items_payment_currency` (`payment_currency_code`)");
  }

  private List<String> tableNames() {
    return jdbcTemplate.queryForList(
        """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                ORDER BY table_name
                """,
        String.class);
  }

  private String columnType(String tableName, String columnName) {
    return jdbcTemplate.queryForObject(
        """
                SELECT column_type
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """,
        String.class,
        tableName,
        columnName);
  }

  private String showCreateTable(String tableName) {
    return jdbcTemplate.query(
        "SHOW CREATE TABLE " + tableName,
        rs -> {
          rs.next();
          return rs.getString("Create Table");
        });
  }

  static boolean isDockerAvailable() {
    return DockerClientFactory.instance().isDockerAvailable();
  }
}
