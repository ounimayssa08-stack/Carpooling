package com.carpooling.service;

import com.carpooling.enums.PaymentStatus;
import com.carpooling.model.Payment;
import com.carpooling.repository.PaymentRepository;

import java.sql.SQLException;
import java.util.Optional;

public class PaymentService {

    private final PaymentRepository paymentRepo = new PaymentRepository();

    /** Creates an AUTHORIZED payment when a reservation is made. */
    public Payment authorizePayment(int reservationId, double amount) throws SQLException {
        Payment p = new Payment(0, reservationId, amount, PaymentStatus.AUTHORIZED);
        paymentRepo.save(p);
        return p;
    }

    /** Captures an AUTHORIZED payment (driver validates trip). */
    public void capturePayment(int reservationId) throws SQLException {
        Optional<Payment> opt = paymentRepo.findByReservation(reservationId);
        if (opt.isEmpty()) throw new IllegalArgumentException("No payment found for reservation.");
        Payment p = opt.get();
        if (p.getStatus() != PaymentStatus.AUTHORIZED)
            throw new IllegalStateException("Payment is not in AUTHORIZED state.");
        paymentRepo.updateStatus(p.getId(), PaymentStatus.CAPTURED);
    }

    /**
     * Processes refund based on hours until trip departure.
     * >24h → Full refund. <=24h → 50% partial refund.
     */
    public void processRefund(int reservationId, long hoursUntilTrip) throws SQLException {
        Optional<Payment> opt = paymentRepo.findByReservation(reservationId);
        if (opt.isEmpty()) return;
        Payment p = opt.get();
        if (p.getStatus() == PaymentStatus.REFUNDED
                || p.getStatus() == PaymentStatus.PARTIAL_REFUND) return;

        if (hoursUntilTrip > 24) {
            paymentRepo.updateStatus(p.getId(), PaymentStatus.REFUNDED);
        } else {
            double refundAmount = p.getAmount() * 0.50;
            paymentRepo.updateStatusAndAmount(p.getId(), PaymentStatus.PARTIAL_REFUND, refundAmount);
        }
    }

    /** Driver cancels trip — full refund with 20% penalty tracked via notification. */
    public void refundForDriverCancellation(int reservationId) throws SQLException {
        Optional<Payment> opt = paymentRepo.findByReservation(reservationId);
        if (opt.isEmpty()) return;
        Payment p = opt.get();
        paymentRepo.updateStatus(p.getId(), PaymentStatus.REFUNDED);
    }

    public Optional<Payment> getPaymentByReservation(int reservationId) throws SQLException {
        return paymentRepo.findByReservation(reservationId);
    }
}
