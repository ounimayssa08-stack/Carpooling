package com.carpooling.enums;

public enum ReservationStatus {
    EN_ATTENTE("En attente"),
    CONFIRMEE("Confirmée"),
    REFUSEE("Refusée"),
    ANNULEE("Annulée");

    private final String displayName;

    ReservationStatus(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
