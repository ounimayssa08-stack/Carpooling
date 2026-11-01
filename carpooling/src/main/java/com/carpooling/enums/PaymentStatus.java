package com.carpooling.enums;

public enum PaymentStatus {
    AUTHORIZED("Authorized"),
    CAPTURED("Captured"),
    REFUNDED("Refunded"),
    PARTIAL_REFUND("Partial Refund");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
