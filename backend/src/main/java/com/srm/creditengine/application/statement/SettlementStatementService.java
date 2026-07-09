package com.srm.creditengine.application.statement;

import com.srm.creditengine.api.request.SettlementStatementRequest;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.infrastructure.query.SettlementStatementQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementStatementService {

  private final ListReferenceDataService referenceDataService;
  private final SettlementStatementQueryRepository settlementStatementQueryRepository;

  public SettlementStatementService(
      ListReferenceDataService referenceDataService,
      SettlementStatementQueryRepository settlementStatementQueryRepository) {
    this.referenceDataService = referenceDataService;
    this.settlementStatementQueryRepository = settlementStatementQueryRepository;
  }

  @Transactional(readOnly = true)
  public Page<SettlementStatementRow> getStatement(SettlementStatementRequest request) {
    SettlementStatementFilter filter =
        SettlementStatementFilter.from(request, referenceDataService);
    return settlementStatementQueryRepository.findStatement(filter);
  }
}
