package com.carpooling.controller;

import com.carpooling.MainApp;
import com.carpooling.model.User;
import com.carpooling.service.AuthService;
import com.carpooling.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        try {
            User user = authService.login(email, password);
            MainApp.showDashboard(user);
        } catch (IllegalArgumentException | IllegalStateException e) {
            AlertUtil.showError("Login Failed", e.getMessage());
            passwordField.clear();
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "An error occurred: " + e.getMessage());
        } catch (java.io.IOException e) {
            AlertUtil.showError("Navigation Error", "Could not open dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterLink(ActionEvent event) {
        try {
            MainApp.showRegister();
        } catch (java.io.IOException e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
