package com.carpooling.controller;

import com.carpooling.MainApp;
import com.carpooling.enums.Role;
import com.carpooling.model.User;
import com.carpooling.service.AuthService;
import com.carpooling.util.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ChoiceBox<Role> roleChoiceBox;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        roleChoiceBox.getItems().addAll(Role.PASSENGER, Role.DRIVER);
        roleChoiceBox.setValue(Role.PASSENGER);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        Role role = roleChoiceBox.getValue();

        if (!password.equals(confirm)) {
            AlertUtil.showError("Validation Error", "Passwords do not match.");
            return;
        }

        try {
            User user = authService.register(name, email, password, role);
            AlertUtil.showInfo("Registration Successful",
                    "Welcome, " + user.getName() + "! Please log in.");
            MainApp.showLogin();
        } catch (IllegalArgumentException e) {
            AlertUtil.showError("Registration Failed", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Database Error", "An error occurred: " + e.getMessage());
        } catch (java.io.IOException e) {
            AlertUtil.showError("Navigation Error", "Could not navigate: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoginLink(ActionEvent event) {
        try {
            MainApp.showLogin();
        } catch (java.io.IOException e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
