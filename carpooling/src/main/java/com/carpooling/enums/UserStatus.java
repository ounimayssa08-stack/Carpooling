package com.carpooling.enums;

public enum UserStatus {
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    BLOCKED("Blocked");

    private final String displayName;

    UserStatus(String displayName) {
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
