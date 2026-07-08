package com.srm.creditengine.api.controller;

import com.srm.creditengine.api.request.SettlementStatementRequest;
import com.srm.creditengine.api.response.PageResponse;
import com.srm.creditengine.api.response.SettlementStatementRowResponse;
import com.srm.creditengine.application.statement.SettlementStatementService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements/statement")
public class SettlementStatementController {

    private final SettlementStatementService settlementStatementService;

    public SettlementStatementController(SettlementStatementService settlementStatementService) {
        this.settlementStatementService = settlementStatementService;
    }

    @GetMapping
    @Operation(summary = "Get the settlement statement")
    public PageResponse<SettlementStatementRowResponse> getStatement(
            @ModelAttribute SettlementStatementRequest request
    ) {
        var resultPage = settlementStatementService.getStatement(request);
        List<SettlementStatementRowResponse> content = resultPage.getContent().stream()
                .map(row -> new SettlementStatementRowResponse(
                        row.settlementId(),
                        row.assignorId(),
                        row.assignorName(),
                        row.assignorDocument(),
                        row.sourceCurrency(),
                        row.paymentCurrency(),
                        row.status(),
                        row.itemCount(),
                        row.totalFaceValue(),
                        row.totalPresentValue(),
                        row.totalPaymentValue(),
                        row.settledAt()
                ))
                .toList();

        return new PageResponse<>(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }
}
