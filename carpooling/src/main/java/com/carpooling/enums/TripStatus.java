package com.carpooling.enums;

public enum TripStatus {
    EN_ATTENTE("En attente"),
    CONFIRME("Confirmé"),
    COMPLET("Complet"),
    EN_COURS("En cours"),
    TERMINE("Terminé"),
    ANNULE("Annulé");

    private final String displayName;

    TripStatus(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
