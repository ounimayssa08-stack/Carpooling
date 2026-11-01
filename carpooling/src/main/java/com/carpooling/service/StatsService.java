package com.carpooling.service;

import com.carpooling.util.DatabaseManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StatsService {

    public Map<String, Object> getGeneralStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            // Users
            stats.put("totalUsers", getCount(conn, "SELECT COUNT(*) FROM users"));
            stats.put("totalDrivers", getCount(conn, "SELECT COUNT(*) FROM users WHERE role='DRIVER'"));
            stats.put("totalPassengers", getCount(conn, "SELECT COUNT(*) FROM users WHERE role='PASSENGER'"));

            // Trips
            stats.put("totalTrips", getCount(conn, "SELECT COUNT(*) FROM trips"));
            stats.put("activeTrips", getCount(conn, "SELECT COUNT(*) FROM trips WHERE status='EN_COURS'"));
            stats.put("completedTrips", getCount(conn, "SELECT COUNT(*) FROM trips WHERE status='TERMINE'"));

            // Reservations
            stats.put("totalReservations", getCount(conn, "SELECT COUNT(*) FROM reservations"));

            // Financial
            stats.put("totalRevenue", getDouble(conn, "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status='CAPTURED'"));
        }
        return stats;
    }

    private int getCount(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private double getDouble(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public Map<String, Integer> getTripsPerDay() throws SQLException {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT date(trip_date) as day, COUNT(*) as count FROM trips GROUP BY day ORDER BY day LIMIT 7";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("day"), rs.getInt("count"));
            }
        }
        return data;
    }
}
