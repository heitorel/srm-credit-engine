package com.srm.creditengine.infrastructure.config;

import com.srm.creditengine.application.startup.StartupTestDataService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "application.startup-test-data",
    name = "enabled",
    havingValue = "true")
public class StartupTestDataInitializer implements ApplicationRunner {

  private final StartupTestDataService startupTestDataService;

  public StartupTestDataInitializer(StartupTestDataService startupTestDataService) {
    this.startupTestDataService = startupTestDataService;
  }

  @Override
  public void run(ApplicationArguments args) {
    startupTestDataService.seed();
  }
}
