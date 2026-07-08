package com.srm.creditengine.application.reference;

public record CurrencyReference(
        String code,
        String name,
        int decimalPlaces
) {
}
