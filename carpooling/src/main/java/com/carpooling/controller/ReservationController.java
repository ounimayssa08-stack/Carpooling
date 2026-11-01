package com.carpooling.controller;

import com.carpooling.model.Payment;
import com.carpooling.model.Reservation;
import com.carpooling.service.PaymentService;
import com.carpooling.service.ReservationService;
import com.carpooling.service.TripService;
import com.carpooling.enums.TripStatus;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReservationController implements DashboardAware {

    @FXML private ListView<Reservation> reservationListView;
    @FXML private Button cancelButton;
    @FXML private Button rateButton;

    private final ReservationService reservationService = new ReservationService();
    private final PaymentService paymentService = new PaymentService();
    private DashboardController dashboardController;

    @Override
    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    @FXML
    public void initialize() {
        reservationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Reservation res, boolean empty) {
                super.updateItem(res, empty);
                if (empty || res == null) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(12);
                    card.getStyleClass().add("trip-card");

                    javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10);
                    header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label routeLbl = new Label("🚗 " + res.getDeparture() + " ➔ " + res.getDestination());
                    routeLbl.getStyleClass().add("card-route-bold");
                    header.getChildren().add(routeLbl);

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    header.getChildren().add(spacer);

                    String amount = "—";
                    try {
                        Optional<Payment> p = paymentService.getPaymentByReservation(res.getId());
                        if (p.isPresent()) amount = com.carpooling.util.CurrencyUtil.formatTND(p.get().getAmount());
                    } catch (Exception e) {}
                    Label priceLbl = new Label(amount);
                    priceLbl.getStyleClass().add("card-price");
                    header.getChildren().add(priceLbl);

                    javafx.scene.layout.HBox footer = new javafx.scene.layout.HBox(20);
                    footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label dateLbl = new Label("📅 " + res.getTripDate());
                    Label driverLbl = new Label("👤 " + res.getDriverName());
                    Label seatsLbl = new Label("🪑 " + res.getSeatsReserved() + " places");
                    Label statusLbl = new Label(res.getStatus().getDisplayName().toUpperCase());
                    
                    dateLbl.getStyleClass().add("card-info-text");
                    driverLbl.getStyleClass().add("card-info-text");
                    seatsLbl.getStyleClass().add("card-info-text");
                    statusLbl.getStyleClass().addAll("card-status-badge", "status-" + res.getStatus().name().toLowerCase());

                    footer.getChildren().addAll(dateLbl, driverLbl, seatsLbl, statusLbl);

                    card.getChildren().addAll(header, footer);
                    setGraphic(card);
                }
            }
        });

        loadReservations();
    }

    private void loadReservations() {
        try {
            int userId = SessionManager.getCurrentUser().getId();
            List<Reservation> list = reservationService.getUserReservations(userId);
            reservationListView.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger les réservations : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Reservation selected = reservationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner une réservation à annuler.");
            return;
        }
        if (!AlertUtil.showConfirm("Annuler la réservation",
                "Annuler la réservation for le trajet de " + selected.getDeparture()
                        + " à " + selected.getDestination() + " ?\n"
                        + "Remboursement complet si >24h avant le départ, 50% sinon."))
            return;

        try {
            int userId = SessionManager.getCurrentUser().getId();
            reservationService.cancelReservation(selected.getId(), userId);
            AlertUtil.showInfo("Annulée", "Réservation annulée et remboursement traité.");
            loadReservations();
            if (dashboardController != null) dashboardController.refreshNotificationBadge();
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertUtil.showError("Erreur", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Erreur Base de Données", e.getMessage());
        }
    }

    @FXML
    private void handleReview(ActionEvent event) {
        Reservation selected = reservationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un trajet pour évaluer le conducteur.");
            return;
        }
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/carpooling/fxml/Review.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ReviewController controller = loader.getController();
            controller.setTripId(selected.getTripId());
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Évaluer le conducteur");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadReservations();
    }
}
