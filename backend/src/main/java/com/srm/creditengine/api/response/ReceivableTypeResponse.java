package com.srm.creditengine.api.response;

import java.math.BigDecimal;

public record ReceivableTypeResponse(String code, String description, BigDecimal monthlySpread) {}
