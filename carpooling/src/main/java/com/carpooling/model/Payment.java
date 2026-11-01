package com.carpooling.model;

import com.carpooling.enums.PaymentStatus;
import java.util.Objects;

public class Payment {
    private int id;
    private int reservationId;
    private double amount;
    private PaymentStatus status;

    public Payment() {}

    public Payment(int id, int reservationId, double amount, PaymentStatus status) {
        this.id = id;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment p)) return false;
        return id == p.id && reservationId == p.reservationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservationId);
    }

    @Override
    public String toString() {
        return "Payment{id=" + id + ", reservationId=" + reservationId
                + ", amount=" + amount + ", status=" + status + "}";
    }
}
