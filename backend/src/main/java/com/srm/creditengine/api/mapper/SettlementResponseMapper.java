package com.srm.creditengine.api.mapper;

import com.srm.creditengine.api.response.AssignorResponse;
import com.srm.creditengine.api.response.SettlementItemResponse;
import com.srm.creditengine.api.response.SettlementResponse;
import com.srm.creditengine.domain.settlement.Assignor;
import com.srm.creditengine.domain.settlement.Settlement;
import com.srm.creditengine.domain.settlement.SettlementItem;
import org.springframework.stereotype.Component;

@Component
public class SettlementResponseMapper {

  public SettlementResponse toResponse(Settlement settlement) {
    return new SettlementResponse(
        settlement.id(),
        toAssignorResponse(settlement.assignor()),
        settlement.sourceCurrency(),
        settlement.paymentCurrency(),
        settlement.status().name(),
        settlement.baseRate(),
        settlement.itemCount(),
        settlement.totalFaceValue(),
        settlement.totalPresentValue(),
        settlement.totalPaymentValue(),
        settlement.settledAt(),
        settlement.items().stream().map(this::toItemResponse).toList());
  }

  private AssignorResponse toAssignorResponse(Assignor assignor) {
    return new AssignorResponse(assignor.id(), assignor.name(), assignor.document());
  }

  private SettlementItemResponse toItemResponse(SettlementItem item) {
    return new SettlementItemResponse(
        item.id(),
        item.receivableId(),
        item.externalReference(),
        item.receivableType(),
        item.faceValue(),
        item.sourceCurrency(),
        item.paymentCurrency(),
        item.baseRate(),
        item.spread(),
        item.termInMonths(),
        item.presentValueInSourceCurrency(),
        item.discountValue(),
        item.paymentValue(),
        item.exchangeRate(),
        item.calculatedAt());
  }
}
