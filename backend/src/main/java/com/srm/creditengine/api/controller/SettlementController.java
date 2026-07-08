package com.srm.creditengine.api.controller;

import com.srm.creditengine.api.mapper.SettlementResponseMapper;
import com.srm.creditengine.api.request.CreateSettlementRequest;
import com.srm.creditengine.api.response.SettlementResponse;
import com.srm.creditengine.application.settlement.CreateSettlementService;
import com.srm.creditengine.application.settlement.GetSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final CreateSettlementService createSettlementService;
    private final GetSettlementService getSettlementService;
    private final SettlementResponseMapper settlementResponseMapper;

    public SettlementController(
            CreateSettlementService createSettlementService,
            GetSettlementService getSettlementService,
            SettlementResponseMapper settlementResponseMapper
    ) {
        this.createSettlementService = createSettlementService;
        this.getSettlementService = getSettlementService;
        this.settlementResponseMapper = settlementResponseMapper;
    }

    @PostMapping
    @Operation(summary = "Create a settlement batch")
    public ResponseEntity<SettlementResponse> create(@Valid @RequestBody CreateSettlementRequest request) {
        var settlement = createSettlementService.create(request);
        return ResponseEntity.created(URI.create("/api/settlements/" + settlement.id()))
                .body(settlementResponseMapper.toResponse(settlement));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get settlement details by id")
    public SettlementResponse getById(@PathVariable UUID id) {
        return settlementResponseMapper.toResponse(getSettlementService.getById(id));
    }
}
