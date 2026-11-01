package com.carpooling.service;

import com.carpooling.model.PaymentMethod;
import com.carpooling.repository.PaymentMethodRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepo = new PaymentMethodRepository();

    public void addPaymentMethod(int userId, String cardNumber, String expiryDate, String cvv) throws SQLException {
        if (cardNumber == null || cardNumber.length() < 12) {
            throw new IllegalArgumentException("Numéro de carte invalide.");
        }
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            throw new IllegalArgumentException("Date d'expiration invalide (MM/YY).");
        }
        if (cvv == null || cvv.length() < 3) {
            throw new IllegalArgumentException("CVV invalide.");
        }

        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        PaymentMethod pm = new PaymentMethod(0, userId, cardNumber, expiryDate, cvv, lastFour, LocalDateTime.now());
        paymentMethodRepo.save(pm);
    }

    public List<PaymentMethod> getUserMethods(int userId) throws SQLException {
        return paymentMethodRepo.findByUser(userId);
    }

    public void removePaymentMethod(int id) throws SQLException {
        paymentMethodRepo.delete(id);
    }

    public boolean hasPaymentMethod(int userId) throws SQLException {
        return paymentMethodRepo.hasPaymentMethod(userId);
    }
}
