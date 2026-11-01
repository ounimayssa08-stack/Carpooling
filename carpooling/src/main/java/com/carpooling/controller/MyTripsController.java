package com.carpooling.controller;

import com.carpooling.enums.TripStatus;
import com.carpooling.model.Trip;
import com.carpooling.service.TripService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyTripsController implements DashboardAware {

    @FXML private ListView<Trip> tripListView;
    @FXML private Button cancelTripBtn;

    private final TripService tripService = new TripService();
    private DashboardController dashboardController;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    @FXML
    public void initialize() {
        tripListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Trip trip, boolean empty) {
                super.updateItem(trip, empty);
                if (empty || trip == null) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(10);
                    card.getStyleClass().add("trip-card");

                    javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10);
                    header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label routeLbl = new Label(trip.getDeparture() + " ➔ " + trip.getDestination());
                    routeLbl.getStyleClass().add("card-route");
                    header.getChildren().add(routeLbl);

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    header.getChildren().add(spacer);

                    Label priceLbl = new Label(com.carpooling.util.CurrencyUtil.formatTND(trip.getPrice()));
                    priceLbl.getStyleClass().add("card-price");
                    header.getChildren().add(priceLbl);

                    javafx.scene.layout.HBox footer = new javafx.scene.layout.HBox(20);
                    footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label dateLbl = new Label("📅 " + trip.getTripDate().format(FMT));
                    Label seatsLbl = new Label("🪑 " + trip.getAvailableSeats() + " places restantes");
                    Label statusLbl = new Label(trip.getStatus().getDisplayName());
                    
                    dateLbl.getStyleClass().add("card-info-text");
                    seatsLbl.getStyleClass().add("card-info-text");
                    statusLbl.getStyleClass().add("card-status");
                    if (trip.getStatus() == com.carpooling.enums.TripStatus.EN_ATTENTE) statusLbl.getStyleClass().add("card-status-active");

                    footer.getChildren().addAll(dateLbl, seatsLbl, statusLbl);

                    card.getChildren().addAll(header, footer);
                    setGraphic(card);
                }
            }
        });

        loadTrips();
    }

    private void loadTrips() {
        try {
            int driverId = SessionManager.getCurrentUser().getId();
            List<Trip> trips = tripService.getDriverTrips(driverId);
            tripListView.setItems(FXCollections.observableArrayList(trips));
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les trajets : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelTrip(ActionEvent event) {
        Trip selected = tripListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un trajet à annuler.");
            return;
        }
        if (selected.getStatus() == TripStatus.ANNULE) {
            AlertUtil.showWarning("Déjà annulé", "Ce trajet est déjà annulé.");
            return;
        }
        if (!AlertUtil.showConfirm("Annuler le trajet",
                "Annuler le trajet de " + selected.getDeparture() + " à " + selected.getDestination()
                        + " ?\nTous les passagers seront informés et remboursés."))
            return;

        try {
            int driverId = SessionManager.getCurrentUser().getId();
            tripService.cancelTrip(selected.getId(), driverId);
            AlertUtil.showInfo("Succès", "Trajet annulé. Les passagers ont été informés.");
            loadTrips();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleManageReservations(ActionEvent event) {
        Trip selected = tripListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un trajet pour gérer ses réservations.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/carpooling/fxml/ManageReservations.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ManageReservationsController controller = loader.getController();
            controller.setTrip(selected);
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Gérer les Réservations");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh trips after managing reservations (seats might have updated)
            loadTrips();
        } catch (java.io.IOException e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre : " + e.getMessage());
        }
    }

    @FXML
    private void handleStartTrip(ActionEvent event) {
        Trip selected = tripListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un trajet à démarrer.");
            return;
        }
        try {
            int driverId = SessionManager.getCurrentUser().getId();
            tripService.startTrip(selected.getId(), driverId);
            AlertUtil.showInfo("Succès", "Trajet démarré !");
            loadTrips();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleEndTrip(ActionEvent event) {
        Trip selected = tripListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un trajet à terminer.");
            return;
        }
        try {
            int driverId = SessionManager.getCurrentUser().getId();
            tripService.endTrip(selected.getId(), driverId);
            AlertUtil.showInfo("Succès", "Trajet terminé !");
            loadTrips();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadTrips();
    }
}
