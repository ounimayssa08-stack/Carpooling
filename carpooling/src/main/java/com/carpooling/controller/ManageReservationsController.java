package com.carpooling.controller;

import com.carpooling.enums.ReservationStatus;
import com.carpooling.model.Reservation;
import com.carpooling.model.Trip;
import com.carpooling.service.ReservationService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ManageReservationsController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> passengerCol;
    @FXML private TableColumn<Reservation, Integer> seatsCol;
    @FXML private TableColumn<Reservation, String> statusCol;

    private final ReservationService reservationService = new ReservationService();
    private Trip currentTrip;

    public void setTrip(Trip trip) {
        this.currentTrip = trip;
        loadReservations();
    }

    @FXML
    public void initialize() {
        passengerCol.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seatsReserved"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadReservations() {
        if (currentTrip == null) return;
        try {
            List<Reservation> reservations = reservationService.getTripReservations(currentTrip.getId());
            reservationTable.setItems(FXCollections.observableArrayList(reservations));
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les réservations : " + e.getMessage());
        }
    }

    @FXML
    private void handleAccept() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Sélectionnez une réservation à accepter.");
            return;
        }
        if (selected.getStatus() != ReservationStatus.EN_ATTENTE) {
            AlertUtil.showWarning("Non valide", "Cette réservation n'est pas en attente.");
            return;
        }

        try {
            int driverId = SessionManager.getCurrentUser().getId();
            reservationService.acceptReservation(selected.getId(), driverId);
            AlertUtil.showInfo("Succès", "Réservation acceptée !");
            loadReservations();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleRefuse() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Aucune sélection", "Sélectionnez une réservation à refuser.");
            return;
        }
        if (selected.getStatus() != ReservationStatus.EN_ATTENTE) {
            AlertUtil.showWarning("Non valide", "Cette réservation n'est pas en attente.");
            return;
        }

        if (!AlertUtil.showConfirm("Confirmer", "Refuser cette réservation ?")) return;

        try {
            int driverId = SessionManager.getCurrentUser().getId();
            reservationService.refuseReservation(selected.getId(), driverId);
            AlertUtil.showInfo("Succès", "Réservation refusée.");
            loadReservations();
        } catch (Exception e) {
            AlertUtil.showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) reservationTable.getScene().getWindow();
        stage.close();
    }
}
