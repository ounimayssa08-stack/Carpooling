package com.carpooling.enums;

public enum Role {
    PASSENGER("Passager"),
    DRIVER("Chauffeur"),
    ADMIN("Administrateur");

    private final String displayName;

    Role(String displayName) {
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
