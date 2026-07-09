package com.srm.creditengine.application.startup;

import com.srm.creditengine.api.request.AssignorRequest;
import com.srm.creditengine.api.request.CreateSettlementRequest;
import com.srm.creditengine.api.request.ReceivableSettlementRequest;
import com.srm.creditengine.application.settlement.CreateSettlementService;
import com.srm.creditengine.domain.exchange.ExchangeRate;
import com.srm.creditengine.domain.shared.Rate;
import com.srm.creditengine.infrastructure.persistence.AssignorEntity;
import com.srm.creditengine.infrastructure.persistence.CurrencyEntity;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableTypeEntity;
import com.srm.creditengine.infrastructure.repository.AssignorJpaRepository;
import com.srm.creditengine.infrastructure.repository.CurrencyJpaRepository;
import com.srm.creditengine.infrastructure.repository.ExchangeRateJpaRepository;
import com.srm.creditengine.infrastructure.repository.ReceivableJpaRepository;
import com.srm.creditengine.infrastructure.repository.ReceivableTypeJpaRepository;
import com.srm.creditengine.infrastructure.repository.SettlementItemJpaRepository;
import com.srm.creditengine.infrastructure.repository.SettlementJpaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartupTestDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StartupTestDataService.class);

  private static final SeedExchangeRateDefinition BRL_USD_PREVIOUS =
      new SeedExchangeRateDefinition(
          "11111111-1111-1111-1111-111111111111", "BRL", "USD", "0.18850000", 3);
  private static final SeedExchangeRateDefinition BRL_USD_LATEST =
      new SeedExchangeRateDefinition(
          "22222222-2222-2222-2222-222222222222", "BRL", "USD", "0.19000000", 1);
  private static final SeedExchangeRateDefinition USD_BRL_PREVIOUS =
      new SeedExchangeRateDefinition(
          "33333333-3333-3333-3333-333333333333", "USD", "BRL", "5.21000000", 3);
  private static final SeedExchangeRateDefinition USD_BRL_LATEST =
      new SeedExchangeRateDefinition(
          "44444444-4444-4444-4444-444444444444", "USD", "BRL", "5.25000000", 1);

  private static final SeedAssignorDefinition LUMEN_ASSIGNOR =
      new SeedAssignorDefinition(
          "55555555-5555-5555-5555-555555555555", "Lumen Atacado Ltda.", "11222333000144");
  private static final SeedAssignorDefinition ACME_ASSIGNOR =
      new SeedAssignorDefinition(
          "66666666-6666-6666-6666-666666666666", "ACME Comercio Ltda.", "12345678000199");
  private static final SeedAssignorDefinition ORBIT_ASSIGNOR =
      new SeedAssignorDefinition(
          "77777777-7777-7777-7777-777777777777", "Orbit Foods S.A.", "99887766000155");
  private static final SeedAssignorDefinition BLUE_ASSIGNOR =
      new SeedAssignorDefinition(
          "88888888-8888-8888-8888-888888888888", "Blue Export LLC", "55667788990011");

  private final AssignorJpaRepository assignorRepository;
  private final CurrencyJpaRepository currencyRepository;
  private final ExchangeRateJpaRepository exchangeRateRepository;
  private final ReceivableJpaRepository receivableRepository;
  private final ReceivableTypeJpaRepository receivableTypeRepository;
  private final SettlementJpaRepository settlementRepository;
  private final SettlementItemJpaRepository settlementItemRepository;
  private final CreateSettlementService createSettlementService;
  private final Clock clock;

  public StartupTestDataService(
      AssignorJpaRepository assignorRepository,
      CurrencyJpaRepository currencyRepository,
      ExchangeRateJpaRepository exchangeRateRepository,
      ReceivableJpaRepository receivableRepository,
      ReceivableTypeJpaRepository receivableTypeRepository,
      SettlementJpaRepository settlementRepository,
      SettlementItemJpaRepository settlementItemRepository,
      CreateSettlementService createSettlementService,
      Clock clock) {
    this.assignorRepository = assignorRepository;
    this.currencyRepository = currencyRepository;
    this.exchangeRateRepository = exchangeRateRepository;
    this.receivableRepository = receivableRepository;
    this.receivableTypeRepository = receivableTypeRepository;
    this.settlementRepository = settlementRepository;
    this.settlementItemRepository = settlementItemRepository;
    this.createSettlementService = createSettlementService;
    this.clock = clock;
  }

  @Transactional
  public void seed() {
    seedExchangeRates();
    seedAvailableReceivables();
    seedSettlements();

    LOGGER.info(
        "Startup test data ready: {} assignors, {} exchange rates, {} receivables, {} settlements, {} settlement items.",
        assignorRepository.count(),
        exchangeRateRepository.count(),
        receivableRepository.count(),
        settlementRepository.count(),
        settlementItemRepository.count());
  }

  private void seedExchangeRates() {
    seedExchangeRate(BRL_USD_PREVIOUS);
    seedExchangeRate(BRL_USD_LATEST);
    seedExchangeRate(USD_BRL_PREVIOUS);
    seedExchangeRate(USD_BRL_LATEST);
  }

  private void seedExchangeRate(SeedExchangeRateDefinition definition) {
    if (exchangeRateRepository.existsById(definition.id())) {
      return;
    }

    CurrencyEntity sourceCurrency =
        currencyRepository.getReferenceById(definition.sourceCurrency());
    CurrencyEntity targetCurrency =
        currencyRepository.getReferenceById(definition.targetCurrency());
    Instant validAt = Instant.now(clock).minus(definition.daysAgo(), ChronoUnit.DAYS);
    Instant createdAt = validAt.plus(5, ChronoUnit.MINUTES);

    ExchangeRate exchangeRate =
        new ExchangeRate(
            UUID.fromString(definition.id()),
            com.srm.creditengine.domain.currency.CurrencyCode.of(definition.sourceCurrency()),
            com.srm.creditengine.domain.currency.CurrencyCode.of(definition.targetCurrency()),
            Rate.positive(new BigDecimal(definition.rate())),
            validAt,
            createdAt);

    exchangeRateRepository.save(
        ExchangeRateEntity.of(exchangeRate, sourceCurrency, targetCurrency));
  }

  private void seedAvailableReceivables() {
    AssignorEntity assignor = resolveAssignor(LUMEN_ASSIGNOR);

    seedAvailableReceivable(
        assignor,
        "99999999-9999-9999-9999-999999999991",
        "LUM-AV-001",
        "MERCANTILE_DUPLICATE",
        "BRL",
        "18000.00",
        LocalDate.now(clock).plusDays(45));
    seedAvailableReceivable(
        assignor,
        "99999999-9999-9999-9999-999999999992",
        "LUM-AV-002",
        "POST_DATED_CHECK",
        "BRL",
        "7500.00",
        LocalDate.now(clock).plusDays(75));
  }

  private void seedAvailableReceivable(
      AssignorEntity assignor,
      String id,
      String externalReference,
      String receivableTypeCode,
      String currencyCode,
      String faceValue,
      LocalDate dueDate) {
    if (receivableRepository
        .findByAssignor_IdAndExternalReference(assignor.getId(), externalReference)
        .isPresent()) {
      return;
    }

    ReceivableTypeEntity receivableType =
        receivableTypeRepository.getReferenceById(receivableTypeCode);
    CurrencyEntity currency = currencyRepository.getReferenceById(currencyCode);
    LocalDateTime now = utcNow();

    receivableRepository.save(
        ReceivableEntity.create(
            id,
            assignor,
            externalReference,
            receivableType,
            new BigDecimal(faceValue),
            currency,
            dueDate,
            "AVAILABLE",
            now,
            now));
  }

  private void seedSettlements() {
    seedSettlement(
        new SeedSettlementDefinition(
            ACME_ASSIGNOR,
            "BRL",
            new BigDecimal("0.01000000"),
            List.of(
                new SeedSettlementItemDefinition(
                    "NF-2001", "10000.00", "BRL", "MERCANTILE_DUPLICATE", 55),
                new SeedSettlementItemDefinition(
                    "CHK-2002", "6500.00", "BRL", "POST_DATED_CHECK", 80))));
    seedSettlement(
        new SeedSettlementDefinition(
            ORBIT_ASSIGNOR,
            "USD",
            new BigDecimal("0.01250000"),
            List.of(
                new SeedSettlementItemDefinition(
                    "NF-3001", "22000.00", "BRL", "MERCANTILE_DUPLICATE", 50),
                new SeedSettlementItemDefinition(
                    "NF-3002", "9500.00", "BRL", "POST_DATED_CHECK", 95))));
    seedSettlement(
        new SeedSettlementDefinition(
            BLUE_ASSIGNOR,
            "BRL",
            new BigDecimal("0.00850000"),
            List.of(
                new SeedSettlementItemDefinition(
                    "INV-4001", "12500.00", "USD", "MERCANTILE_DUPLICATE", 70))));
  }

  private void seedSettlement(SeedSettlementDefinition definition) {
    Optional<AssignorEntity> assignor =
        assignorRepository.findFirstByDocument(definition.assignor().document());
    long existingItems =
        assignor.stream()
            .flatMap(
                entity ->
                    definition.receivables().stream()
                        .map(
                            item ->
                                receivableRepository.findByAssignor_IdAndExternalReference(
                                    entity.getId(), item.externalReference())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .count();

    if (existingItems == definition.receivables().size()) {
      return;
    }

    if (existingItems > 0) {
      throw new IllegalStateException(
          "Startup settlement seed for assignor document %s is partially present."
              .formatted(definition.assignor().document()));
    }

    createSettlementService.create(
        new CreateSettlementRequest(
            new AssignorRequest(definition.assignor().name(), definition.assignor().document()),
            definition.paymentCurrency(),
            definition.baseRate(),
            definition.receivables().stream()
                .map(
                    item ->
                        new ReceivableSettlementRequest(
                            item.externalReference(),
                            new BigDecimal(item.faceValue()),
                            item.sourceCurrency(),
                            item.receivableType(),
                            LocalDate.now(clock).plusDays(item.dueInDays())))
                .toList()));
  }

  private AssignorEntity resolveAssignor(SeedAssignorDefinition definition) {
    return assignorRepository
        .findFirstByDocument(definition.document())
        .orElseGet(
            () -> {
              LocalDateTime now = utcNow();
              return assignorRepository.save(
                  AssignorEntity.create(
                      definition.id(), definition.name(), definition.document(), now, now));
            });
  }

  private LocalDateTime utcNow() {
    return LocalDateTime.ofInstant(Instant.now(clock), ZoneOffset.UTC);
  }

  private record SeedExchangeRateDefinition(
      String id, String sourceCurrency, String targetCurrency, String rate, long daysAgo) {}

  private record SeedAssignorDefinition(String id, String name, String document) {}

  private record SeedSettlementDefinition(
      SeedAssignorDefinition assignor,
      String paymentCurrency,
      BigDecimal baseRate,
      List<SeedSettlementItemDefinition> receivables) {}

  private record SeedSettlementItemDefinition(
      String externalReference,
      String faceValue,
      String sourceCurrency,
      String receivableType,
      long dueInDays) {}
}
