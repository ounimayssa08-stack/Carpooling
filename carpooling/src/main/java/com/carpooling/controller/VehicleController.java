package com.carpooling.controller;

import com.carpooling.model.Vehicle;
import com.carpooling.service.VehicleService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class VehicleController implements DashboardAware {

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String> brandCol;
    @FXML private TableColumn<Vehicle, String> modelCol;
    @FXML private TableColumn<Vehicle, String> plateCol;
    @FXML private TableColumn<Vehicle, Integer> seatsCol;

    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField plateField;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;

    private final VehicleService vehicleService = new VehicleService();
    private Vehicle selectedVehicle = null;
    private DashboardController dashboardController;

    @Override
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        plateCol.setCellValueFactory(new PropertyValueFactory<>("plate"));
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));

        seatsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 4));

        vehicleTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> populateForm(selected));

        loadVehicles();
    }

    private void loadVehicles() {
        try {
            int driverId = SessionManager.getCurrentUser().getId();
            List<Vehicle> vehicles = vehicleService.getDriverVehicles(driverId);
            vehicleTable.setItems(FXCollections.observableArrayList(vehicles));
        } catch (SQLException e) {
            AlertUtil.showError("Error", "Could not load vehicles: " + e.getMessage());
        }
    }

    private void populateForm(Vehicle v) {
        if (v == null) return;
        selectedVehicle = v;
        brandField.setText(v.getBrand());
        modelField.setText(v.getModel());
        plateField.setText(v.getPlate());
        seatsSpinner.getValueFactory().setValue(v.getSeats());
        saveButton.setText("Update Vehicle");
        deleteButton.setDisable(false);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String plate = plateField.getText().trim().toUpperCase();
        int seats = seatsSpinner.getValue();

        try {
            if (selectedVehicle == null) {
                int driverId = SessionManager.getCurrentUser().getId();
                vehicleService.addVehicle(driverId, model, brand, plate, seats);
                AlertUtil.showInfo("Success", "Vehicle added successfully.");
            } else {
                vehicleService.updateVehicle(selectedVehicle, model, brand, plate, seats);
                AlertUtil.showInfo("Success", "Vehicle updated successfully.");
            }
            clearForm();
            loadVehicles();
        } catch (IllegalArgumentException | SQLException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedVehicle == null) return;
        if (!AlertUtil.showConfirm("Delete Vehicle",
                "Delete vehicle " + selectedVehicle.getModel() + " (" + selectedVehicle.getPlate() + ")?"))
            return;

        try {
            vehicleService.deleteVehicle(selectedVehicle.getId());
            AlertUtil.showInfo("Success", "Vehicle deleted.");
            clearForm();
            loadVehicles();
        } catch (SQLException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private void clearForm() {
        selectedVehicle = null;
        brandField.clear();
        modelField.clear();
        plateField.clear();
        seatsSpinner.getValueFactory().setValue(4);
        saveButton.setText("Add Vehicle");
        deleteButton.setDisable(true);
        vehicleTable.getSelectionModel().clearSelection();
    }
}
