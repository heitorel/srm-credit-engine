package com.srm.creditengine.api.controller;

import com.srm.creditengine.api.response.CurrenciesResponse;
import com.srm.creditengine.api.response.CurrencyResponse;
import com.srm.creditengine.api.response.ReceivableTypeResponse;
import com.srm.creditengine.api.response.ReceivableTypesResponse;
import com.srm.creditengine.application.reference.ListReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference-data")
public class ReferenceDataController {

  private final ListReferenceDataService listReferenceDataService;

  public ReferenceDataController(ListReferenceDataService listReferenceDataService) {
    this.listReferenceDataService = listReferenceDataService;
  }

  @GetMapping("/currencies")
  @Operation(summary = "List supported currencies")
  public CurrenciesResponse listCurrencies() {
    List<CurrencyResponse> currencies =
        listReferenceDataService.listCurrencies().stream()
            .map(
                currency ->
                    new CurrencyResponse(
                        currency.code(), currency.name(), currency.decimalPlaces()))
            .toList();
    return new CurrenciesResponse(currencies);
  }

  @GetMapping("/receivable-types")
  @Operation(summary = "List supported receivable types")
  public ReceivableTypesResponse listReceivableTypes() {
    List<ReceivableTypeResponse> receivableTypes =
        listReferenceDataService.listReceivableTypes().stream()
            .map(
                receivableType ->
                    new ReceivableTypeResponse(
                        receivableType.code(),
                        receivableType.description(),
                        receivableType.monthlySpread()))
            .toList();
    return new ReceivableTypesResponse(receivableTypes);
  }
}
