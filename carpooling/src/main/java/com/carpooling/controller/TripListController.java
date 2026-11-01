package com.carpooling.controller;

import com.carpooling.enums.Role;
import com.carpooling.model.Trip;
import com.carpooling.service.ReviewService;
import com.carpooling.service.ReservationService;
import com.carpooling.service.TripService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TripListController implements DashboardAware {

    @FXML private TextField departureSearchField;
    @FXML private TextField destinationSearchField;
    @FXML private ListView<Trip> tripListView;
    @FXML private Spinner<Integer> seatsToReserveSpinner;
    @FXML private Button reserveButton;
    @FXML private Button cancelTripButton;

    private final TripService tripService = new TripService();
    private final ReservationService reservationService = new ReservationService();
    private final ReviewService reviewService = new ReviewService();
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
                    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(12);
                    card.getStyleClass().add("trip-card");

                    // Header: Driver info + Rating
                    javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10);
                    header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    String ratingText = "⭐ N/A";
                    try {
                        double[] stats = reviewService.getDriverStats(trip.getDriverId());
                        if (stats[1] > 0) ratingText = String.format("⭐ %.1f (%d avis)", stats[0], (int)stats[1]);
                    } catch (Exception e) {}

                    Label driverLbl = new Label("👤 " + trip.getDriverName());
                    driverLbl.getStyleClass().add("card-driver-name");
                    Label ratingLbl = new Label(ratingText);
                    ratingLbl.getStyleClass().add("card-rating");

                    header.getChildren().addAll(driverLbl, ratingLbl);

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    header.getChildren().add(spacer);

                    Label priceLbl = new Label(com.carpooling.util.CurrencyUtil.formatTND(trip.getPrice()));
                    priceLbl.getStyleClass().add("card-price");
                    header.getChildren().add(priceLbl);

                    // Body: Route (Bold)
                    Label routeLbl = new Label(trip.getDeparture() + " ➔ " + trip.getDestination());
                    routeLbl.getStyleClass().add("card-route-bold");

                    // Info: Date + Vehicle
                    javafx.scene.layout.VBox infoBox = new javafx.scene.layout.VBox(5);
                    Label dateLbl = new Label("📅 " + trip.getTripDate().format(FMT));
                    Label vehicleLbl = new Label("🚗 " + trip.getVehicleModel() + " (" + trip.getVehiclePlate() + ")");
                    dateLbl.getStyleClass().add("card-info-text");
                    vehicleLbl.getStyleClass().add("card-info-text");
                    infoBox.getChildren().addAll(dateLbl, vehicleLbl);

                    // Footer: Seats + Status Badge
                    javafx.scene.layout.HBox footer = new javafx.scene.layout.HBox(20);
                    footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label seatsLbl = new Label("🪑 " + trip.getAvailableSeats() + " places restantes");
                    Label statusLbl = new Label(trip.getStatus().getDisplayName().toUpperCase());
                    
                    seatsLbl.getStyleClass().add("card-info-text");
                    statusLbl.getStyleClass().addAll("card-status-badge", "status-" + trip.getStatus().name().toLowerCase());

                    footer.getChildren().addAll(seatsLbl, statusLbl);

                    card.getChildren().addAll(header, routeLbl, infoBox, footer);
                    setGraphic(card);
                }
            }
        });

        tripListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateReserveButtonState(newVal);
        });

        seatsToReserveSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        loadAllTrips();
    }

    private void updateReserveButtonState(Trip trip) {
        if (trip == null) {
            reserveButton.setDisable(true);
            reserveButton.setText("Réserver");
            return;
        }

        boolean isDisabled = false;
        String btnText = "Réserver";

        if (trip.getAvailableSeats() <= 0 || trip.getStatus() == com.carpooling.enums.TripStatus.COMPLET) {
            isDisabled = true;
            btnText = "Complet";
        } else if (trip.getStatus() == com.carpooling.enums.TripStatus.EN_COURS) {
            isDisabled = true;
            btnText = "En cours";
        } else if (trip.getStatus() == com.carpooling.enums.TripStatus.TERMINE) {
            isDisabled = true;
            btnText = "Terminé";
        } else {
            try {
                int userId = SessionManager.getCurrentUser().getId();
                if (reservationService.hasActiveReservation(userId, trip.getId())) {
                    isDisabled = true;
                    btnText = "Déjà réservé";
                } else if (trip.getDriverId() == userId) {
                    isDisabled = true;
                    btnText = "Votre trajet";
                }
            } catch (Exception e) {}
        }

        reserveButton.setDisable(isDisabled);
        reserveButton.setText(btnText);
    }

    private void loadAllTrips() {
        try {
            List<Trip> trips = tripService.getAllTrips();
            tripListView.setItems(FXCollections.observableArrayList(trips));
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les trajets : " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String dep = departureSearchField.getText().trim();
        String dst = destinationSearchField.getText().trim();
        try {
            List<Trip> trips = tripService.searchTrips(dep, dst);
            tripListView.setItems(FXCollections.observableArrayList(trips));
        } catch (SQLException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleClearSearch(ActionEvent event) {
        departureSearchField.clear();
        destinationSearchField.clear();
        loadAllTrips();
    }

    @FXML
    private void handleReserve(ActionEvent event) {
        Trip selected = tripListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un trajet à réserver.");
            return;
        }
        int seats = seatsToReserveSpinner.getValue();
        if (!AlertUtil.showConfirm("Confirmer la réservation",
                "Réserver " + seats + " place(s) pour le trajet de " + selected.getDeparture()
                        + " à " + selected.getDestination() + " ?\nTotal: "
                        + com.carpooling.util.CurrencyUtil.formatTND(seats * selected.getPrice())))
            return;

        try {
            int userId = SessionManager.getCurrentUser().getId();
            reservationService.reserve(userId, selected.getId(), seats);
            AlertUtil.showInfo("Succès", "Demande de réservation envoyée au conducteur !");
            loadAllTrips();
            if (dashboardController != null) dashboardController.refreshNotificationBadge();
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (e.getMessage().contains("moyen de paiement")) {
                openPaymentMethodWindow();
            } else {
                AlertUtil.showError("Échec de la réservation", e.getMessage());
            }
        } catch (SQLException e) {
            AlertUtil.showError("Erreur Base de Données", e.getMessage());
        }
    }

    private void openPaymentMethodWindow() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/carpooling/fxml/PaymentMethod.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Ajouter un moyen de paiement");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir la fenêtre de paiement.");
        }
    }

    @FXML
    private void handleCancelTrip(ActionEvent event) {
        Trip selected = tripListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a trip to cancel.");
            return;
        }
        if (!AlertUtil.showConfirm("Cancel Trip",
                "Are you sure you want to cancel this trip?\nAll passengers will be notified."))
            return;

        try {
            int driverId = SessionManager.getCurrentUser().getId();
            tripService.cancelTrip(selected.getId(), driverId);
            AlertUtil.showInfo("Success", "Trip cancelled. Passengers have been notified.");
            loadAllTrips();
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertUtil.showError("Error", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleViewProfile(ActionEvent event) {
        Trip selected = tripListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un trajet pour voir le profil du conducteur.");
            return;
        }

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/carpooling/fxml/DriverProfile.fxml"));
            javafx.scene.Parent root = loader.load();
            
            DriverProfileController controller = loader.getController();
            controller.setDriverId(selected.getDriverId());
            
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Profil du Conducteur");
            stage.setScene(new javafx.scene.Scene(root));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le profil : " + e.getMessage());
        }
    }
}
