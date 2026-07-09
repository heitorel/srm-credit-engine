package com.srm.creditengine.application.settlement;

import com.srm.creditengine.api.error.ApiErrorDetail;
import com.srm.creditengine.api.request.AssignorRequest;
import com.srm.creditengine.api.request.CreateSettlementRequest;
import com.srm.creditengine.api.request.ReceivableSettlementRequest;
import com.srm.creditengine.application.pricing.BaseRateResolver;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.domain.currency.CurrencyCode;
import com.srm.creditengine.domain.pricing.PricingContext;
import com.srm.creditengine.domain.pricing.PricingEngine;
import com.srm.creditengine.domain.pricing.PricingResult;
import com.srm.creditengine.domain.receivable.ReceivableStatus;
import com.srm.creditengine.domain.receivable.ReceivableType;
import com.srm.creditengine.domain.settlement.Assignor;
import com.srm.creditengine.domain.settlement.Settlement;
import com.srm.creditengine.domain.settlement.SettlementItem;
import com.srm.creditengine.domain.settlement.SettlementStatus;
import com.srm.creditengine.domain.shared.FinancialMath;
import com.srm.creditengine.domain.shared.Money;
import com.srm.creditengine.domain.shared.Rate;
import com.srm.creditengine.domain.shared.Term;
import com.srm.creditengine.infrastructure.persistence.AssignorEntity;
import com.srm.creditengine.infrastructure.persistence.CurrencyEntity;
import com.srm.creditengine.infrastructure.persistence.ExchangeRateEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableEntity;
import com.srm.creditengine.infrastructure.persistence.ReceivableTypeEntity;
import com.srm.creditengine.infrastructure.persistence.SettlementEntity;
import com.srm.creditengine.infrastructure.persistence.SettlementItemEntity;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSettlementService {

  private final AssignorJpaRepository assignorRepository;
  private final ReceivableJpaRepository receivableRepository;
  private final SettlementJpaRepository settlementRepository;
  private final SettlementItemJpaRepository settlementItemRepository;
  private final CurrencyJpaRepository currencyRepository;
  private final ReceivableTypeJpaRepository receivableTypeRepository;
  private final ExchangeRateJpaRepository exchangeRateRepository;
  private final ListReferenceDataService referenceDataService;
  private final BaseRateResolver baseRateResolver;
  private final PricingEngine pricingEngine;
  private final FinancialMath financialMath;
  private final Clock clock;

  public CreateSettlementService(
      AssignorJpaRepository assignorRepository,
      ReceivableJpaRepository receivableRepository,
      SettlementJpaRepository settlementRepository,
      SettlementItemJpaRepository settlementItemRepository,
      CurrencyJpaRepository currencyRepository,
      ReceivableTypeJpaRepository receivableTypeRepository,
      ExchangeRateJpaRepository exchangeRateRepository,
      ListReferenceDataService referenceDataService,
      BaseRateResolver baseRateResolver,
      PricingEngine pricingEngine,
      FinancialMath financialMath,
      Clock clock) {
    this.assignorRepository = assignorRepository;
    this.receivableRepository = receivableRepository;
    this.settlementRepository = settlementRepository;
    this.settlementItemRepository = settlementItemRepository;
    this.currencyRepository = currencyRepository;
    this.receivableTypeRepository = receivableTypeRepository;
    this.exchangeRateRepository = exchangeRateRepository;
    this.referenceDataService = referenceDataService;
    this.baseRateResolver = baseRateResolver;
    this.pricingEngine = pricingEngine;
    this.financialMath = financialMath;
    this.clock = clock;
  }

  @Transactional
  public Settlement create(CreateSettlementRequest request) {
    validateDuplicateReferences(request.receivables());

    CurrencyCode paymentCurrency =
        referenceDataService.requireSupportedCurrency(request.paymentCurrency(), "paymentCurrency");
    Rate baseRate = baseRateResolver.resolve(request.baseRate());
    Instant settlementInstant = Instant.now(clock);
    LocalDate pricingDate = LocalDate.now(clock);
    LocalDateTime settlementDateTime = toUtcDateTime(settlementInstant);

    AssignorEntity assignorEntity = resolveAssignor(request.assignor(), settlementDateTime);
    CurrencyCode sourceCurrency = validateAndResolveCommonSourceCurrency(request.receivables());

    CurrencyEntity sourceCurrencyEntity =
        currencyRepository.getReferenceById(sourceCurrency.value());
    CurrencyEntity paymentCurrencyEntity =
        currencyRepository.getReferenceById(paymentCurrency.value());

    List<ResolvedItem> resolvedItems = new ArrayList<>();

    for (int index = 0; index < request.receivables().size(); index++) {
      ReceivableSettlementRequest itemRequest = request.receivables().get(index);
      resolvedItems.add(
          resolveItem(
              assignorEntity,
              itemRequest,
              index,
              sourceCurrency,
              paymentCurrency,
              baseRate,
              pricingDate,
              settlementInstant,
              settlementDateTime));
    }

    try {
      receivableRepository.saveAll(
          resolvedItems.stream().map(ResolvedItem::receivableEntity).toList());

      SettlementEntity settlementEntity =
          settlementRepository.save(
              SettlementEntity.create(
                  UUID.randomUUID().toString(),
                  assignorEntity,
                  sourceCurrencyEntity,
                  paymentCurrencyEntity,
                  SettlementStatus.SETTLED.name(),
                  financialMath.roundRate(baseRate.value()),
                  resolvedItems.size(),
                  totalFaceValue(resolvedItems),
                  totalPresentValue(resolvedItems),
                  totalPaymentValue(resolvedItems),
                  settlementDateTime,
                  settlementDateTime,
                  settlementDateTime));

      List<SettlementItemEntity> savedItems =
          settlementItemRepository.saveAll(
              resolvedItems.stream()
                  .map(item -> item.toSettlementItemEntity(settlementEntity, paymentCurrencyEntity))
                  .toList());

      return toDomainSettlement(settlementEntity, savedItems);
    } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException exception) {
      throw new DuplicateSettlementException();
    }
  }

  private AssignorEntity resolveAssignor(AssignorRequest request, LocalDateTime now) {
    String normalizedDocument = normalize(request.document());
    Optional<AssignorEntity> existing =
        normalizedDocument == null
            ? Optional.empty()
            : assignorRepository.findFirstByDocument(normalizedDocument);

    return existing.orElseGet(
        () ->
            assignorRepository.save(
                AssignorEntity.create(
                    UUID.randomUUID().toString(),
                    request.name().trim(),
                    normalizedDocument,
                    now,
                    now)));
  }

  private CurrencyCode validateAndResolveCommonSourceCurrency(
      List<ReceivableSettlementRequest> receivables) {
    Set<String> uniqueCurrencies = new HashSet<>();
    CurrencyCode resolvedSourceCurrency = null;

    for (int index = 0; index < receivables.size(); index++) {
      CurrencyCode currency =
          referenceDataService.requireSupportedCurrency(
              receivables.get(index).sourceCurrency(),
              "receivables[%d].sourceCurrency".formatted(index));
      uniqueCurrencies.add(currency.value());
      if (resolvedSourceCurrency == null) {
        resolvedSourceCurrency = currency;
      }
    }

    if (uniqueCurrencies.size() > 1) {
      throw new MixedSourceCurrencyException();
    }

    return resolvedSourceCurrency;
  }

  private ResolvedItem resolveItem(
      AssignorEntity assignorEntity,
      ReceivableSettlementRequest request,
      int itemIndex,
      CurrencyCode sourceCurrency,
      CurrencyCode paymentCurrency,
      Rate baseRate,
      LocalDate pricingDate,
      Instant calculatedAt,
      LocalDateTime calculatedAtDateTime) {
    Money faceValue =
        Money.positive(
            request.faceValue(),
            sourceCurrency,
            "receivables[%d].faceValue".formatted(itemIndex),
            "Face value");
    ReceivableType.from(request.receivableType());
    ReceivableTypeEntity receivableTypeEntity =
        receivableTypeRepository
            .findById(request.receivableType())
            .orElseThrow(
                () ->
                    new com.srm.creditengine.domain.pricing.UnsupportedReceivableTypeException(
                        request.receivableType()));

    ReceivableEntity receivableEntity =
        receivableRepository
            .findByAssignor_IdAndExternalReference(
                assignorEntity.getId(), request.externalReference())
            .map(existing -> validateExistingReceivable(existing, request, itemIndex))
            .orElseGet(
                () ->
                    ReceivableEntity.create(
                        UUID.randomUUID().toString(),
                        assignorEntity,
                        request.externalReference().trim(),
                        receivableTypeEntity,
                        request.faceValue(),
                        currencyRepository.getReferenceById(sourceCurrency.value()),
                        request.dueDate(),
                        ReceivableStatus.AVAILABLE.name(),
                        calculatedAtDateTime,
                        calculatedAtDateTime));

    validateReceivableStatus(assignorEntity, receivableEntity, itemIndex);

    Term term = Term.between(pricingDate, request.dueDate());
    Rate exchangeRate =
        sourceCurrency.value().equals(paymentCurrency.value())
            ? null
            : exchangeRateRepository
                .findFirstBySourceCurrency_CodeAndTargetCurrency_CodeOrderByValidAtDescCreatedAtDescIdDesc(
                    sourceCurrency.value(), paymentCurrency.value())
                .map(ExchangeRateEntity::toResult)
                .map(result -> Rate.positive(result.rate(), "paymentCurrency", "Exchange rate"))
                .orElse(null);

    PricingResult pricingResult =
        pricingEngine.price(
            new PricingContext(
                faceValue,
                paymentCurrency,
                baseRate,
                request.receivableType(),
                term,
                exchangeRate,
                calculatedAt));

    receivableEntity.markAsSettled(calculatedAtDateTime);

    return new ResolvedItem(
        receivableEntity, receivableTypeEntity, pricingResult, calculatedAtDateTime);
  }

  private ReceivableEntity validateExistingReceivable(
      ReceivableEntity existing, ReceivableSettlementRequest request, int itemIndex) {
    List<ApiErrorDetail> mismatches = new ArrayList<>();

    if (existing.getFaceValue().compareTo(request.faceValue()) != 0) {
      mismatches.add(
          new ApiErrorDetail(
              "receivables[%d].faceValue".formatted(itemIndex),
              "Face value does not match the existing receivable."));
    }
    if (!existing.getCurrency().getCode().equals(request.sourceCurrency())) {
      mismatches.add(
          new ApiErrorDetail(
              "receivables[%d].sourceCurrency".formatted(itemIndex),
              "Source currency does not match the existing receivable."));
    }
    if (!existing.getReceivableType().getCode().equals(request.receivableType())) {
      mismatches.add(
          new ApiErrorDetail(
              "receivables[%d].receivableType".formatted(itemIndex),
              "Receivable type does not match the existing receivable."));
    }
    if (!existing.getDueDate().equals(request.dueDate())) {
      mismatches.add(
          new ApiErrorDetail(
              "receivables[%d].dueDate".formatted(itemIndex),
              "Due date does not match the existing receivable."));
    }

    if (!mismatches.isEmpty()) {
      throw new InvalidSettlementBatchException(
          "Settlement request contains receivable data that conflicts with existing records.",
          mismatches);
    }

    return existing;
  }

  private void validateReceivableStatus(
      AssignorEntity assignorEntity, ReceivableEntity receivableEntity, int itemIndex) {
    ReceivableStatus status = ReceivableStatus.from(receivableEntity.getStatus());
    if (status == ReceivableStatus.SETTLED) {
      throw new DuplicateSettlementException(
          receivableEntity.getExternalReference(), itemIndex, assignorIdentifier(assignorEntity));
    }
    if (status == ReceivableStatus.CANCELLED) {
      throw new InvalidSettlementBatchException(
          "Only AVAILABLE receivables may be settled.",
          List.of(
              new ApiErrorDetail(
                  "receivables[%d].externalReference".formatted(itemIndex),
                  "Cancelled receivables cannot be settled.")));
    }
  }

  private void validateDuplicateReferences(List<ReceivableSettlementRequest> receivables) {
    Set<String> seenReferences = new HashSet<>();
    for (int index = 0; index < receivables.size(); index++) {
      String reference = receivables.get(index).externalReference().trim();
      if (!seenReferences.add(reference)) {
        throw new InvalidSettlementBatchException(
            "Settlement batch contains duplicate receivables.",
            List.of(
                new ApiErrorDetail(
                    "receivables[%d].externalReference".formatted(index),
                    "Duplicate receivable references are not allowed in the same request.")));
      }
    }
  }

  private BigDecimal totalFaceValue(List<ResolvedItem> items) {
    return financialMath.roundMoney(
        items.stream()
            .map(item -> item.pricingResult().faceValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  private BigDecimal totalPresentValue(List<ResolvedItem> items) {
    return financialMath.roundMoney(
        items.stream()
            .map(item -> item.pricingResult().presentValueInSourceCurrency())
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  private BigDecimal totalPaymentValue(List<ResolvedItem> items) {
    return financialMath.roundMoney(
        items.stream()
            .map(item -> item.pricingResult().netPaymentValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  private Settlement toDomainSettlement(
      SettlementEntity settlementEntity, List<SettlementItemEntity> itemEntities) {
    return new Settlement(
        UUID.fromString(settlementEntity.getId()),
        new Assignor(
            UUID.fromString(settlementEntity.getAssignor().getId()),
            settlementEntity.getAssignor().getName(),
            settlementEntity.getAssignor().getDocument()),
        settlementEntity.getSourceCurrency().getCode(),
        settlementEntity.getPaymentCurrency().getCode(),
        SettlementStatus.valueOf(settlementEntity.getStatus()),
        settlementEntity.getBaseRate(),
        settlementEntity.getItemCount(),
        settlementEntity.getTotalFaceValue(),
        settlementEntity.getTotalPresentValue(),
        settlementEntity.getTotalPaymentValue(),
        settlementEntity.getSettledAt().toInstant(ZoneOffset.UTC),
        itemEntities.stream().map(this::toDomainItem).toList());
  }

  private SettlementItem toDomainItem(SettlementItemEntity itemEntity) {
    return new SettlementItem(
        UUID.fromString(itemEntity.getId()),
        UUID.fromString(itemEntity.getReceivable().getId()),
        itemEntity.getExternalReference(),
        itemEntity.getReceivableType().getCode(),
        itemEntity.getFaceValue(),
        itemEntity.getSourceCurrency().getCode(),
        itemEntity.getPaymentCurrency().getCode(),
        itemEntity.getBaseRate(),
        itemEntity.getSpread(),
        itemEntity.getTermInMonths(),
        itemEntity.getPresentValueSource(),
        itemEntity.getDiscountValue(),
        itemEntity.getPaymentValue(),
        itemEntity.getExchangeRate(),
        itemEntity.getCalculatedAt().toInstant(ZoneOffset.UTC));
  }

  private String assignorIdentifier(AssignorEntity assignorEntity) {
    return assignorEntity.getDocument() != null
        ? assignorEntity.getDocument()
        : assignorEntity.getName();
  }

  private static LocalDateTime toUtcDateTime(Instant instant) {
    return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
  }

  private static String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private record ResolvedItem(
      ReceivableEntity receivableEntity,
      ReceivableTypeEntity receivableTypeEntity,
      PricingResult pricingResult,
      LocalDateTime createdAt) {
    SettlementItemEntity toSettlementItemEntity(
        SettlementEntity settlementEntity, CurrencyEntity paymentCurrencyEntity) {
      return SettlementItemEntity.create(
          UUID.randomUUID().toString(),
          settlementEntity,
          receivableEntity,
          receivableEntity.getExternalReference(),
          receivableTypeEntity,
          pricingResult.faceValue(),
          receivableEntity.getCurrency(),
          paymentCurrencyEntity,
          pricingResult.baseRate(),
          pricingResult.spread(),
          pricingResult.termInMonths(),
          pricingResult.presentValueInSourceCurrency(),
          pricingResult.discountValue(),
          pricingResult.netPaymentValue(),
          pricingResult.exchangeRate(),
          toUtcDateTime(pricingResult.calculatedAt()),
          createdAt);
    }
  }
}
