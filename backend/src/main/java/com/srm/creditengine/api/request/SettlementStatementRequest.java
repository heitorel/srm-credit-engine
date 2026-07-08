package com.srm.creditengine.api.request;

import java.time.LocalDate;
import java.util.UUID;

public record SettlementStatementRequest(
        LocalDate from,
        LocalDate to,
        UUID assignorId,
        String assignorDocument,
        String paymentCurrency,
        String sourceCurrency,
        String receivableType,
        String status,
        Integer page,
        Integer size,
        String sort
) {
}
