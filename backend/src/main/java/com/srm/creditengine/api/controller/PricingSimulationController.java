package com.srm.creditengine.api.controller;

import com.srm.creditengine.api.request.PricingSimulationRequest;
import com.srm.creditengine.api.response.PricingSimulationResponse;
import com.srm.creditengine.application.pricing.PricingSimulationService;
import com.srm.creditengine.domain.pricing.PricingResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingSimulationController {

    private final PricingSimulationService pricingSimulationService;

    public PricingSimulationController(PricingSimulationService pricingSimulationService) {
        this.pricingSimulationService = pricingSimulationService;
    }

    @PostMapping("/simulations")
    @Operation(summary = "Simulate receivable pricing")
    public PricingSimulationResponse simulate(@Valid @RequestBody PricingSimulationRequest request) {
        return toResponse(pricingSimulationService.simulate(request));
    }

    private static PricingSimulationResponse toResponse(PricingResult result) {
        return new PricingSimulationResponse(
                result.faceValue(),
                result.sourceCurrency(),
                result.paymentCurrency(),
                result.presentValueInSourceCurrency(),
                result.netPaymentValue(),
                result.discountValue(),
                result.baseRate(),
                result.spread(),
                result.termInMonths(),
                result.exchangeRate(),
                result.calculatedAt()
        );
    }
}
