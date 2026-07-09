package com.srm.creditengine.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MigrationScriptsTest {

  private static final Path MIGRATIONS_DIR = Path.of("src", "main", "resources", "db", "migration");

  @Test
  void shouldDefineAllExpectedTablesUsingDecimalSafeTypes() throws IOException {
    String migration = Files.readString(MIGRATIONS_DIR.resolve("V1__create_initial_schema.sql"));

    assertThat(migration).contains("CREATE TABLE currencies");
    assertThat(migration).contains("CREATE TABLE receivable_types");
    assertThat(migration).contains("CREATE TABLE assignors");
    assertThat(migration).contains("CREATE TABLE exchange_rates");
    assertThat(migration).contains("CREATE TABLE receivables");
    assertThat(migration).contains("CREATE TABLE settlements");
    assertThat(migration).contains("CREATE TABLE settlement_items");
    assertThat(migration).contains("DECIMAL(19, 4)");
    assertThat(migration).contains("DECIMAL(19, 8)");
    assertThat(migration).contains("UNIQUE (assignor_id, external_reference)");
    assertThat(migration).contains("UNIQUE (receivable_id)");
    assertThat(migration).contains("idx_settlements_statement_default");
    assertThat(migration).doesNotContain("FLOAT");
    assertThat(migration).doesNotContain("DOUBLE");
    assertThat(migration).doesNotContain("REAL");
  }

  @Test
  void shouldSeedSupportedCurrenciesAndReceivableTypes() throws IOException {
    String seedMigration = Files.readString(MIGRATIONS_DIR.resolve("V2__seed_reference_data.sql"));

    assertThat(seedMigration).contains("'BRL', 'Brazilian Real', 2");
    assertThat(seedMigration).contains("'USD', 'US Dollar', 2");
    assertThat(seedMigration)
        .contains("'MERCANTILE_DUPLICATE', 'Mercantile Duplicate', 0.01500000");
    assertThat(seedMigration).contains("'POST_DATED_CHECK', 'Post-Dated Check', 0.02500000");
  }
}
