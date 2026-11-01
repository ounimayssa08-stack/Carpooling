package com.carpooling.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class PaymentMethod {
    private int id;
    private int userId;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardLastFour;
    private LocalDateTime createdAt;

    public PaymentMethod() {}

    public PaymentMethod(int id, int userId, String cardNumber, String expiryDate,
                         String cvv, String cardLastFour, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardLastFour = cardLastFour;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Display-safe masked card number */
    public String getMaskedCard() {
        return "**** **** **** " + cardLastFour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentMethod pm)) return false;
        return id == pm.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "PaymentMethod{id=" + id + ", card=****" + cardLastFour
                + ", expiry=" + expiryDate + "}";
    }
}
