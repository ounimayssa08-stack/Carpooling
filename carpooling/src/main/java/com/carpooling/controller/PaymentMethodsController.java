package com.carpooling.controller;

import com.carpooling.model.PaymentMethod;
import com.carpooling.service.PaymentMethodService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class PaymentMethodsController {

    @FXML private ListView<PaymentMethod> cardListView;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;

    private final PaymentMethodService paymentService = new PaymentMethodService();

    @FXML
    public void initialize() {
        cardListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PaymentMethod pm, boolean empty) {
                super.updateItem(pm, empty);
                if (empty || pm == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(15);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    box.getStyleClass().add("card-item");

                    Label cardLbl = new Label("💳 " + pm.getMaskedCard());
                    cardLbl.getStyleClass().add("card-text");
                    
                    Label expiryLbl = new Label("Exp: " + pm.getExpiryDate());
                    expiryLbl.getStyleClass().add("text-muted");

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    Button deleteBtn = new Button("Supprimer");
                    deleteBtn.getStyleClass().add("btn-danger-small");
                    deleteBtn.setOnAction(e -> handleDelete(pm));

                    box.getChildren().addAll(cardLbl, expiryLbl, spacer, deleteBtn);
                    setGraphic(box);
                }
            }
        });
        loadCards();
    }

    private void loadCards() {
        try {
            int userId = SessionManager.getCurrentUser().getId();
            List<PaymentMethod> methods = paymentService.getUserMethods(userId);
            cardListView.setItems(FXCollections.observableArrayList(methods));
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les cartes.");
        }
    }

    @FXML
    private void handleAddCard() {
        String number = cardNumberField.getText().replaceAll("\\s+", "");
        String expiry = expiryField.getText();
        String cvv = cvvField.getText();

        try {
            int userId = SessionManager.getCurrentUser().getId();
            paymentService.addPaymentMethod(userId, number, expiry, cvv);
            AlertUtil.showInfo("Succès", "Moyen de paiement ajouté !");
            cardNumberField.clear();
            expiryField.clear();
            cvvField.clear();
            loadCards();
        } catch (IllegalArgumentException e) {
            AlertUtil.showWarning("Validation", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible d'ajouter la carte.");
        }
    }

    private void handleDelete(PaymentMethod pm) {
        if (AlertUtil.showConfirm("Supprimer", "Voulez-vous vraiment supprimer ce moyen de paiement ?")) {
            try {
                paymentService.removePaymentMethod(pm.getId());
                loadCards();
            } catch (SQLException e) {
                AlertUtil.showError("Erreur", "Impossible de supprimer la carte.");
            }
        }
    }
}
