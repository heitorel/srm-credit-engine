package com.srm.creditengine.application.statement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.srm.creditengine.api.request.SettlementStatementRequest;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import com.srm.creditengine.infrastructure.query.SettlementStatementQueryRepository;
import org.junit.jupiter.api.Test;

class SettlementStatementServiceTest {

  @Test
  void shouldRejectInvalidStatementDateRangeBeforeQueryingRepository() {
    SettlementStatementService service =
        new SettlementStatementService(
            mock(ListReferenceDataService.class), mock(SettlementStatementQueryRepository.class));

    assertThatThrownBy(
            () ->
                service.getStatement(
                    new SettlementStatementRequest(
                        java.time.LocalDate.parse("2026-07-09"),
                        java.time.LocalDate.parse("2026-07-08"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null)))
        .isInstanceOf(InvalidStatementDateRangeException.class);
  }
}
