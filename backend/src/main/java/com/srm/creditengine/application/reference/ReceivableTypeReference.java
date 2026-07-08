package com.srm.creditengine.application.reference;

import java.math.BigDecimal;

public record ReceivableTypeReference(
        String code,
        String description,
        BigDecimal monthlySpread
) {
}
