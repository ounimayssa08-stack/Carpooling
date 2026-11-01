package com.carpooling.controller;

import com.carpooling.MainApp;
import com.carpooling.enums.Role;
import com.carpooling.model.Notification;
import com.carpooling.model.User;
import com.carpooling.service.AuthService;
import com.carpooling.service.NotificationService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label notificationBadge;
    @FXML private StackPane contentArea;

    // Sidebar buttons (visibility toggled per role)
    @FXML private Button tripListBtn;
    @FXML private Button myReservationsBtn;
    @FXML private Button createTripBtn;
    @FXML private Button myTripsBtn;
    @FXML private Button vehiclesBtn;
    @FXML private Button reviewsBtn;
    @FXML private Button paymentMethodsBtn;
    @FXML private Button sidebarNotifBtn;
    @FXML private Button myAccountBtn;
    @FXML private Button adminStatsBtn;
    @FXML private Button adminBtn;
    
    @FXML private Label driverSectionLabel;
    @FXML private Label clientSectionLabel;
    @FXML private Separator sidebarSeparator;

    private final AuthService authService = new AuthService();
    private final NotificationService notificationService = new NotificationService();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        userNameLabel.setText(user.getName());
        userRoleLabel.setText(user.getRole().getDisplayName());

        configureMenuForRole(user.getRole());
        refreshNotificationBadge();

        // Load default view
        switch (user.getRole()) {
            case ADMIN -> handleAdmin();
            case DRIVER -> handleMyTrips();
            default -> handleTripList();
        }
    }

    private void configureMenuForRole(Role role) {
        // Driver Section visibility
        boolean isDriver = (role == Role.DRIVER);
        driverSectionLabel.setVisible(isDriver);
        driverSectionLabel.setManaged(isDriver);
        sidebarSeparator.setVisible(isDriver);
        sidebarSeparator.setManaged(isDriver);
        createTripBtn.setVisible(isDriver);
        createTripBtn.setManaged(isDriver);
        myTripsBtn.setVisible(isDriver);
        myTripsBtn.setManaged(isDriver);
        vehiclesBtn.setVisible(isDriver);
        vehiclesBtn.setManaged(isDriver);

        // Client Section (Always visible for Driver and Passenger)
        boolean isUser = (role == Role.PASSENGER || role == Role.DRIVER);
        clientSectionLabel.setVisible(isUser);
        clientSectionLabel.setManaged(isUser);
        tripListBtn.setVisible(isUser);
        myReservationsBtn.setVisible(isUser);
        reviewsBtn.setVisible(isUser);
        paymentMethodsBtn.setVisible(isUser);
        sidebarNotifBtn.setVisible(isUser);

        // Account is visible for everyone
        myAccountBtn.setVisible(true);

        // Specific wording for driver
        if (role == Role.DRIVER) {
            tripListBtn.setText("🚗  Réserver un Trajet");
        } else {
            tripListBtn.setText("🔍  Parcourir les Trajets");
        }

        // Admin
        boolean isAdmin = (role == Role.ADMIN);
        adminBtn.setVisible(isAdmin);
        adminBtn.setManaged(isAdmin);
        adminStatsBtn.setVisible(isAdmin);
        adminStatsBtn.setManaged(isAdmin);
    }

    public void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/carpooling/fxml/" + fxmlFile));
            Node view = loader.load();

            // Inject dashboard reference if controller supports it
            Object ctrl = loader.getController();
            if (ctrl instanceof DashboardAware aware) {
                aware.setDashboardController(this);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", "Could not load view: " + e.getMessage());
        }
    }

    private void setActiveButton(Button btn) {
        // Reset all
        tripListBtn.getStyleClass().remove("sidebar-btn-active");
        myReservationsBtn.getStyleClass().remove("sidebar-btn-active");
        paymentMethodsBtn.getStyleClass().remove("sidebar-btn-active");
        sidebarNotifBtn.getStyleClass().remove("sidebar-btn-active");
        createTripBtn.getStyleClass().remove("sidebar-btn-active");
        myTripsBtn.getStyleClass().remove("sidebar-btn-active");
        vehiclesBtn.getStyleClass().remove("sidebar-btn-active");
        reviewsBtn.getStyleClass().remove("sidebar-btn-active");
        myAccountBtn.getStyleClass().remove("sidebar-btn-active");
        adminStatsBtn.getStyleClass().remove("sidebar-btn-active");
        adminBtn.getStyleClass().remove("sidebar-btn-active");

        if (btn != null) btn.getStyleClass().add("sidebar-btn-active");
    }

    @FXML private void handleTripList() { 
        setActiveButton(tripListBtn);
        loadView("TripList.fxml"); 
    }
    @FXML private void handleMyReservations() { 
        setActiveButton(myReservationsBtn);
        loadView("MyReservations.fxml"); 
    }
    @FXML private void handleCreateTrip() { 
        setActiveButton(createTripBtn);
        loadView("CreateTrip.fxml"); 
    }
    @FXML private void handleMyTrips() { 
        setActiveButton(myTripsBtn);
        loadView("MyTrips.fxml"); 
    }
    @FXML private void handleVehicles() { 
        setActiveButton(vehiclesBtn);
        loadView("VehicleManagement.fxml"); 
    }
    @FXML private void handleReviews() { 
        setActiveButton(reviewsBtn);
        // We can load MyReservations or a specific view if exists
        loadView("MyReservations.fxml");
    }
    @FXML private void handleAdmin() { 
        setActiveButton(adminBtn);
        loadView("AdminPanel.fxml"); 
    }
    @FXML private void handlePaymentMethods() {
        setActiveButton(paymentMethodsBtn);
        loadView("PaymentMethods.fxml");
    }
    @FXML private void handleNotificationsView() {
        setActiveButton(sidebarNotifBtn);
        loadView("Notifications.fxml");
    }
    @FXML private void handleAccount() {
        setActiveButton(myAccountBtn);
        loadView("Account.fxml");
    }
    @FXML private void handleAdminStats() {
        setActiveButton(adminStatsBtn);
        loadView("AdminStats.fxml");
    }

    @FXML
    private void handleNotifications() {
        try {
            int userId = SessionManager.getCurrentUser().getId();
            List<Notification> notifications = notificationService.getUserNotifications(userId);
            notificationService.markAllRead(userId);
            notificationBadge.setVisible(false);

            StringBuilder sb = new StringBuilder();
            if (notifications.isEmpty()) {
                sb.append("No notifications.");
            } else {
                for (Notification n : notifications) {
                    sb.append("[").append(n.getType()).append("] ")
                            .append(n.getSentAt().toLocalDate()).append(": ")
                            .append(n.getMessage()).append("\n\n");
                }
            }
            AlertUtil.showInfo("Notifications", sb.toString());
        } catch (SQLException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirm("Logout", "Are you sure you want to log out?")) {
            authService.logout();
            try {
                MainApp.showLogin();
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    public void refreshNotificationBadge() {
        try {
            int count = notificationService.getUnreadCount(
                    SessionManager.getCurrentUser().getId());
            notificationBadge.setVisible(count > 0);
            notificationBadge.setText(String.valueOf(count));
        } catch (SQLException e) {
            notificationBadge.setVisible(false);
        }
    }
}
