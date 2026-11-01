package com.carpooling.controller;

import com.carpooling.model.Vehicle;
import com.carpooling.service.TripService;
import com.carpooling.service.VehicleService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TripController implements DashboardAware {

    @FXML private TextField departureField;
    @FXML private TextField destinationField;
    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> timeHourSpinner;
    @FXML private Spinner<Integer> timeMinuteSpinner;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private TextField priceField;
    @FXML private ChoiceBox<Vehicle> vehicleChoiceBox;
    @FXML private Button createButton;

    private final TripService tripService = new TripService();
    private final VehicleService vehicleService = new VehicleService();
    private DashboardController dashboardController;

    @Override
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        timeHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        timeMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        datePicker.setValue(LocalDate.now().plusDays(1));
        loadVehicles();
    }

    private void loadVehicles() {
        try {
            int driverId = SessionManager.getCurrentUser().getId();
            List<Vehicle> vehicles = vehicleService.getDriverVehicles(driverId);
            vehicleChoiceBox.setItems(FXCollections.observableArrayList(vehicles));
            if (!vehicles.isEmpty()) vehicleChoiceBox.setValue(vehicles.get(0));
        } catch (SQLException e) {
            AlertUtil.showError("Error", "Could not load vehicles: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreate(ActionEvent event) {
        String departure = departureField.getText().trim();
        String destination = destinationField.getText().trim();
        LocalDate date = datePicker.getValue();
        int hour = timeHourSpinner.getValue();
        int minute = timeMinuteSpinner.getValue();
        int seats = seatsSpinner.getValue();
        Vehicle vehicle = vehicleChoiceBox.getValue();

        if (vehicle == null) {
            AlertUtil.showError("Validation", "Please add a vehicle first.");
            return;
        }
        if (date == null) {
            AlertUtil.showError("Validation", "Please select a date.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            AlertUtil.showError("Validation", "Price must be a valid number.");
            return;
        }

        LocalDateTime tripDate = LocalDateTime.of(date, LocalTime.of(hour, minute));

        try {
            int driverId = SessionManager.getCurrentUser().getId();
            tripService.createTrip(driverId, vehicle.getId(), departure, destination,
                    tripDate, seats, price);
            AlertUtil.showInfo("Success", "Trip created successfully!");
            clearForm();
            if (dashboardController != null) dashboardController.loadView("MyTrips.fxml");
        } catch (IllegalArgumentException | SQLException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    private void clearForm() {
        departureField.clear();
        destinationField.clear();
        datePicker.setValue(LocalDate.now().plusDays(1));
        seatsSpinner.getValueFactory().setValue(1);
        priceField.clear();
        timeHourSpinner.getValueFactory().setValue(8);
        timeMinuteSpinner.getValueFactory().setValue(0);
    }
}
