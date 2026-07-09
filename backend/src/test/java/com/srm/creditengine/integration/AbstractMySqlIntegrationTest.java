package com.srm.creditengine.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

public abstract class AbstractMySqlIntegrationTest {

  protected static final MySQLContainer<?> MYSQL =
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

  protected static boolean isDockerAvailable() {
    return DockerClientFactory.instance().isDockerAvailable();
  }
}
