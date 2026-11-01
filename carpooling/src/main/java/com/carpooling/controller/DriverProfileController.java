package com.carpooling.controller;

import com.carpooling.model.Review;
import com.carpooling.model.User;
import com.carpooling.repository.ReviewRepository;
import com.carpooling.repository.UserRepository;
import com.carpooling.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class DriverProfileController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label ratingLabel;
    @FXML private Label reviewCountLabel;

    @FXML private TableView<Review> reviewTable;
    @FXML private TableColumn<Review, String> passengerCol;
    @FXML private TableColumn<Review, Integer> ratingCol;
    @FXML private TableColumn<Review, String> commentCol;

    private final UserRepository userRepo = new UserRepository();
    private final ReviewRepository reviewRepo = new ReviewRepository();

    @FXML
    public void initialize() {
        passengerCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));

        ratingCol.setCellFactory(column -> new javafx.scene.control.TableCell<Review, Integer>() {
            @Override
            protected void updateItem(Integer rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    setText("★".repeat(rating) + "☆".repeat(5 - rating));
                    setStyle("-fx-text-fill: #FB8C00; -fx-font-weight: bold;");
                }
            }
        });
    }

    public void setDriverId(int driverId) {
        try {
            User driver = userRepo.findById(driverId)
                    .orElseThrow(() -> new IllegalArgumentException("Conducteur introuvable."));
            
            nameLabel.setText(driver.getName());
            emailLabel.setText(driver.getEmail());

            double avg = reviewRepo.getAverageRating(driverId);
            int count = reviewRepo.getReviewCount(driverId);

            ratingLabel.setText(String.format("%.1f ⭐", avg));
            reviewCountLabel.setText("(" + count + " avis)");

            List<Review> reviews = reviewRepo.findByDriver(driverId);
            reviewTable.setItems(FXCollections.observableArrayList(reviews));

        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger le profil : " + e.getMessage());
            closeWindow();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        if (nameLabel.getScene() != null) {
            Stage stage = (Stage) nameLabel.getScene().getWindow();
            stage.close();
        }
    }
}
