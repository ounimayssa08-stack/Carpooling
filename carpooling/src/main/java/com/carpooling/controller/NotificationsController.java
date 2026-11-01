package com.carpooling.controller;

import com.carpooling.model.Notification;
import com.carpooling.service.NotificationService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class NotificationsController implements DashboardAware {

    @FXML private ListView<Notification> notificationListView;
    
    private final NotificationService notificationService = new NotificationService();
    private DashboardController dashboardController;

    @Override
    public void setDashboardController(DashboardController dc) {
        this.dashboardController = dc;
    }

    @FXML
    public void initialize() {
        notificationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    box.getStyleClass().add("notification-item");
                    if (!n.isRead()) box.getStyleClass().add("notification-unread");

                    Label typeLbl = new Label(n.getType());
                    typeLbl.getStyleClass().add("notification-type");
                    
                    Label msgLbl = new Label(n.getMessage());
                    msgLbl.setWrapText(true);
                    msgLbl.getStyleClass().add("notification-message");

                    Label dateLbl = new Label(n.getSentAt().toString().replace("T", " "));
                    dateLbl.getStyleClass().add("text-muted-small");

                    box.getChildren().addAll(typeLbl, msgLbl, dateLbl);
                    setGraphic(box);
                }
            }
        });
        loadNotifications();
    }

    private void loadNotifications() {
        try {
            int userId = SessionManager.getCurrentUser().getId();
            List<Notification> list = notificationService.getUserNotifications(userId);
            notificationListView.setItems(FXCollections.observableArrayList(list));
            
            // Mark all as read when opening this view
            notificationService.markAllRead(userId);
            if (dashboardController != null) dashboardController.refreshNotificationBadge();
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les notifications.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadNotifications();
    }
}
