package com.carpooling.controller;

import com.carpooling.model.PaymentMethod;
import com.carpooling.repository.PaymentMethodRepository;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class PaymentMethodController {

    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;

    private final PaymentMethodRepository paymentMethodRepo = new PaymentMethodRepository();

    @FXML
    private void handleSavePayment() {
        String card = cardNumberField.getText();
        String expiry = expiryField.getText();
        String cvv = cvvField.getText();

        if (card == null || card.length() < 16) {
            AlertUtil.showError("Erreur", "Numéro de carte invalide.");
            return;
        }
        if (expiry == null || !expiry.matches("(0[1-9]|1[0-2])\\/\\d{2}")) {
            AlertUtil.showError("Erreur", "Date d'expiration invalide (MM/YY).");
            return;
        }
        if (cvv == null || cvv.length() < 3) {
            AlertUtil.showError("Erreur", "CVV invalide.");
            return;
        }

        String lastFour = card.substring(card.length() - 4);
        PaymentMethod pm = new PaymentMethod(0, SessionManager.getCurrentUser().getId(), card, expiry, cvv, lastFour, LocalDateTime.now());

        try {
            paymentMethodRepo.save(pm);
            AlertUtil.showInfo("Succès", "Moyen de paiement ajouté avec succès.");
            closeWindow();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur Base de Données", "Impossible d'ajouter le moyen de paiement.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cardNumberField.getScene().getWindow();
        stage.close();
    }
}
