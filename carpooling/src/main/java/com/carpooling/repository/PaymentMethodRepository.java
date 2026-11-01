package com.carpooling.repository;

import com.carpooling.model.PaymentMethod;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodRepository {

    public void save(PaymentMethod pm) throws SQLException {
        String sql = "INSERT INTO payment_methods (user_id, card_number, expiry_date, cvv, card_last_four, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pm.getUserId());
            ps.setString(2, pm.getCardNumber());
            ps.setString(3, pm.getExpiryDate());
            ps.setString(4, pm.getCvv());
            ps.setString(5, pm.getCardLastFour());
            ps.setString(6, pm.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) pm.setId(keys.getInt(1));
            }
        }
    }

    public List<PaymentMethod> findByUser(int userId) throws SQLException {
        List<PaymentMethod> list = new ArrayList<>();
        String sql = "SELECT * FROM payment_methods WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PaymentMethod(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("card_number") != null ? rs.getString("card_number") : "",
                            rs.getString("expiry_date"),
                            rs.getString("cvv") != null ? rs.getString("cvv") : "",
                            rs.getString("card_last_four"),
                            LocalDateTime.parse(rs.getString("created_at"))
                    ));
                }
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM payment_methods WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean hasPaymentMethod(int userId) throws SQLException {
        String sql = "SELECT 1 FROM payment_methods WHERE user_id = ? LIMIT 1";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
