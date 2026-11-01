package com.carpooling.controller;

import com.carpooling.service.StatsService;
import com.carpooling.util.AlertUtil;
import com.carpooling.util.CurrencyUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.util.Map;

public class AdminStatsController {

    @FXML private Label totalRevenueLbl;
    @FXML private Label totalTripsLbl;
    @FXML private Label activeTripsLbl;
    @FXML private Label totalUsersLbl;
    @FXML private Label totalReservationsLbl;

    @FXML private BarChart<String, Number> tripsChart;
    @FXML private PieChart usersPieChart;

    private final StatsService statsService = new StatsService();

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            Map<String, Object> stats = statsService.getGeneralStats();
            
            Double revenue = (Double) stats.get("totalRevenue");
            totalRevenueLbl.setText(CurrencyUtil.formatTND(revenue != null ? revenue : 0.0));
            totalTripsLbl.setText(stats.get("totalTrips").toString());
            activeTripsLbl.setText(stats.get("activeTrips").toString());
            totalUsersLbl.setText(stats.get("totalUsers").toString());
            totalReservationsLbl.setText(stats.get("totalReservations").toString());

            // Bar Chart: Trips per day
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Trajets");
            Map<String, Integer> tripData = statsService.getTripsPerDay();
            tripData.forEach((day, count) -> series.getData().add(new XYChart.Data<>(day, count)));
            tripsChart.getData().add(series);

            // Pie Chart: User distribution
            int drivers = (int) stats.get("totalDrivers");
            int passengers = (int) stats.get("totalPassengers");
            usersPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Chauffeurs (" + drivers + ")", drivers),
                    new PieChart.Data("Passagers (" + passengers + ")", passengers)
            ));

        } catch (SQLException e) {
            AlertUtil.showError("Erreur", "Impossible de charger les statistiques.");
        }
    }
}
