package com.srm.creditengine.api.controller;

import com.srm.creditengine.api.request.CreateExchangeRateRequest;
import com.srm.creditengine.api.response.ExchangeRateResponse;
import com.srm.creditengine.application.exchange.CreateExchangeRateService;
import com.srm.creditengine.application.exchange.ExchangeRateResult;
import com.srm.creditengine.application.exchange.GetLatestExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final CreateExchangeRateService createExchangeRateService;
    private final GetLatestExchangeRateService getLatestExchangeRateService;

    public ExchangeRateController(
            CreateExchangeRateService createExchangeRateService,
            GetLatestExchangeRateService getLatestExchangeRateService
    ) {
        this.createExchangeRateService = createExchangeRateService;
        this.getLatestExchangeRateService = getLatestExchangeRateService;
    }

    @PostMapping
    @Operation(summary = "Create an exchange rate")
    public ResponseEntity<ExchangeRateResponse> create(@Valid @RequestBody CreateExchangeRateRequest request) {
        ExchangeRateResult result = createExchangeRateService.create(request);
        return ResponseEntity.created(URI.create("/api/exchange-rates/" + result.id()))
                .body(toResponse(result));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get the latest exchange rate for an exact currency pair")
    public ExchangeRateResponse getLatest(
            @RequestParam String sourceCurrency,
            @RequestParam String targetCurrency
    ) {
        return toResponse(getLatestExchangeRateService.getLatest(sourceCurrency, targetCurrency));
    }

    private static ExchangeRateResponse toResponse(ExchangeRateResult result) {
        return new ExchangeRateResponse(
                result.id(),
                result.sourceCurrency(),
                result.targetCurrency(),
                result.rate(),
                result.validAt(),
                result.createdAt()
        );
    }
}
