package com.carpooling.controller;

import com.carpooling.enums.TripStatus;
import com.carpooling.model.Review;
import com.carpooling.model.Trip;
import com.carpooling.repository.ReviewRepository;
import com.carpooling.service.ReviewService;
import com.carpooling.service.TripService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ReviewController {

    @FXML private ComboBox<Integer> ratingBox;
    @FXML private TextArea commentArea;

    private final ReviewService reviewService = new ReviewService();
    private final TripService tripService = new TripService();
    private Trip currentTrip;

    @FXML
    public void initialize() {
        ratingBox.getItems().addAll(1, 2, 3, 4, 5);
        ratingBox.setValue(5);
    }

    public void setTripId(int tripId) {
        try {
            currentTrip = tripService.getTripById(tripId);
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Trajet introuvable.");
            closeWindow();
        }
    }

    @FXML
    private void handleSubmit() {
        if (currentTrip == null) return;
        
        int rating = ratingBox.getValue();
        String comment = commentArea.getText();

        int userId = SessionManager.getCurrentUser().getId();

        try {
            reviewService.submitReview(userId, currentTrip.getId(), rating, comment);
            AlertUtil.showInfo("Succès", "Merci pour votre évaluation !");
            closeWindow();
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertUtil.showWarning("Attention", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de sauvegarder l'évaluation.");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) ratingBox.getScene().getWindow();
        stage.close();
    }
}
