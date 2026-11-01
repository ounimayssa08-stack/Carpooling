package com.carpooling.controller;

import com.carpooling.enums.UserStatus;
import com.carpooling.model.Trip;
import com.carpooling.model.User;
import com.carpooling.repository.UserRepository;
import com.carpooling.service.TripService;
import com.carpooling.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminController implements DashboardAware {

    // Users Tab
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, String> userRoleCol;
    @FXML private TableColumn<User, String> userStatusCol;
    @FXML private TableColumn<User, Integer> userAttemptsCol;
    @FXML private Button suspendBtn;
    @FXML private Button activateBtn;

    // Trips Tab
    @FXML private TableView<Trip> tripTable;
    @FXML private TableColumn<Trip, String> tripDriverCol;
    @FXML private TableColumn<Trip, String> tripDepartureCol;
    @FXML private TableColumn<Trip, String> tripDestinationCol;
    @FXML private TableColumn<Trip, String> tripDateCol;
    @FXML private TableColumn<Trip, Integer> tripSeatsCol;
    @FXML private TableColumn<Trip, String> tripStatusCol;

    private final UserRepository userRepo = new UserRepository();
    private final TripService tripService = new TripService();
    private DashboardController dashboardController;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    @FXML
    public void initialize() {
        setupUserTable();
        setupTripTable();
        loadUsers();
        loadTrips();
    }

    private void setupUserTable() {
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole().getDisplayName()));
        userStatusCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        userAttemptsCol.setCellValueFactory(new PropertyValueFactory<>("failedAttempts"));
    }

    private void setupTripTable() {
        tripDriverCol.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        tripDepartureCol.setCellValueFactory(new PropertyValueFactory<>("departure"));
        tripDestinationCol.setCellValueFactory(new PropertyValueFactory<>("destination"));
        tripDateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTripDate().format(FMT)));
        tripSeatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        tripStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadUsers() {
        try {
            List<User> users = userRepo.findAll();
            userTable.setItems(FXCollections.observableArrayList(users));
        } catch (SQLException e) {
            AlertUtil.showError("Error", "Could not load users: " + e.getMessage());
        }
    }

    private void loadTrips() {
        try {
            List<Trip> trips = tripService.getAllTrips();
            tripTable.setItems(FXCollections.observableArrayList(trips));
        } catch (SQLException e) {
            AlertUtil.showError("Error", "Could not load trips: " + e.getMessage());
        }
    }

    @FXML
    private void handleSuspend(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a user.");
            return;
        }
        if (selected.getRole().name().equals("ADMIN")) {
            AlertUtil.showError("Error", "Cannot suspend an admin account.");
            return;
        }
        if (!AlertUtil.showConfirm("Suspend User",
                "Suspend " + selected.getName() + " (" + selected.getEmail() + ")?"))
            return;

        try {
            userRepo.updateStatus(selected.getId(), UserStatus.SUSPENDED);
            AlertUtil.showInfo("Done", "User suspended.");
            loadUsers();
        } catch (SQLException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleActivate(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a user.");
            return;
        }
        try {
            userRepo.updateStatus(selected.getId(), UserStatus.ACTIVE);
            userRepo.resetFailedAttempts(selected.getId());
            AlertUtil.showInfo("Done", "User activated.");
            loadUsers();
        } catch (SQLException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML private void handleRefreshUsers(ActionEvent event) { loadUsers(); }
    @FXML private void handleRefreshTrips(ActionEvent event) { loadTrips(); }
}
