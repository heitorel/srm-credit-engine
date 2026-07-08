package com.srm.creditengine.domain.receivable;

public enum ReceivableStatus {
    AVAILABLE,
    SETTLED,
    CANCELLED;

    public static ReceivableStatus from(String rawValue) {
        return valueOf(rawValue);
    }
}
