package com.carpooling.controller;

import com.carpooling.model.User;
import com.carpooling.repository.UserRepository;
import com.carpooling.service.ReviewService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.PasswordUtil;
import com.carpooling.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class AccountController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label roleLabel;
    @FXML private Label ratingLabel;
    @FXML private VBox driverStatsBox;

    private final UserRepository userRepo = new UserRepository();
    private final ReviewService reviewService = new ReviewService();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        roleLabel.setText("Rôle: " + user.getRole().getDisplayName());

        if (user.getRole() == com.carpooling.enums.Role.DRIVER) {
            driverStatsBox.setVisible(true);
            driverStatsBox.setManaged(true);
            try {
                double[] stats = reviewService.getDriverStats(user.getId());
                ratingLabel.setText(String.format("%.1f ⭐ (%d avis)", stats[0], (int)stats[1]));
            } catch (SQLException e) {
                ratingLabel.setText("N/A");
            }
        } else {
            driverStatsBox.setVisible(false);
            driverStatsBox.setManaged(false);
        }
    }

    @FXML
    private void handleUpdateProfile() {
        User user = SessionManager.getCurrentUser();
        String newName = nameField.getText();
        String newEmail = emailField.getText();
        String newPassword = passwordField.getText();

        if (newName.isBlank() || newEmail.isBlank()) {
            AlertUtil.showWarning("Champs requis", "Le nom et l'email ne peuvent pas être vides.");
            return;
        }

        try {
            user.setName(newName);
            user.setEmail(newEmail);
            if (!newPassword.isBlank()) {
                user.setPassword(PasswordUtil.hash(newPassword));
            }
            
            userRepo.update(user);
            AlertUtil.showInfo("Succès", "Profil mis à jour avec succès !");
        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de mettre à jour le profil: " + e.getMessage());
        }
    }
}
