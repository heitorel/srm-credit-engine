package com.srm.creditengine.application.settlement;

import com.srm.creditengine.domain.settlement.Assignor;
import com.srm.creditengine.domain.settlement.Settlement;
import com.srm.creditengine.domain.settlement.SettlementItem;
import com.srm.creditengine.domain.settlement.SettlementStatus;
import com.srm.creditengine.infrastructure.persistence.SettlementEntity;
import com.srm.creditengine.infrastructure.persistence.SettlementItemEntity;
import com.srm.creditengine.infrastructure.repository.SettlementItemJpaRepository;
import com.srm.creditengine.infrastructure.repository.SettlementJpaRepository;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSettlementService {

  private final SettlementJpaRepository settlementRepository;
  private final SettlementItemJpaRepository settlementItemRepository;

  public GetSettlementService(
      SettlementJpaRepository settlementRepository,
      SettlementItemJpaRepository settlementItemRepository) {
    this.settlementRepository = settlementRepository;
    this.settlementItemRepository = settlementItemRepository;
  }

  @Transactional(readOnly = true)
  public Settlement getById(UUID id) {
    SettlementEntity settlementEntity =
        settlementRepository.findById(id.toString()).orElseThrow(SettlementNotFoundException::new);

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
        settlementItemRepository.findBySettlement_IdOrderByCreatedAtAscIdAsc(id.toString()).stream()
            .map(this::toSettlementItem)
            .toList());
  }

  private SettlementItem toSettlementItem(SettlementItemEntity itemEntity) {
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
}
