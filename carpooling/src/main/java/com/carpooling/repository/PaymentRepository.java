package com.carpooling.repository;

import com.carpooling.enums.PaymentStatus;
import com.carpooling.model.Payment;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {

    public void save(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (reservation_id, amount, status) VALUES (?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getReservationId());
            ps.setDouble(2, p.getAmount());
            ps.setString(3, p.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
        }
    }

    public void updateStatus(int paymentId, PaymentStatus status) throws SQLException {
        String sql = "UPDATE payments SET status=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, paymentId);
            ps.executeUpdate();
        }
    }

    public void updateStatusAndAmount(int paymentId, PaymentStatus status, double amount) throws SQLException {
        String sql = "UPDATE payments SET status=?, amount=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setDouble(2, amount);
            ps.setInt(3, paymentId);
            ps.executeUpdate();
        }
    }

    public Optional<Payment> findByReservation(int reservationId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE reservation_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Payment> findByReservationList(List<Integer> reservationIds) throws SQLException {
        List<Payment> list = new ArrayList<>();
        if (reservationIds.isEmpty()) return list;
        StringBuilder sb = new StringBuilder("SELECT * FROM payments WHERE reservation_id IN (");
        for (int i = 0; i < reservationIds.size(); i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        sb.append(")");
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sb.toString())) {
            for (int i = 0; i < reservationIds.size(); i++) {
                ps.setInt(i + 1, reservationIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Payment map(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getInt("reservation_id"),
                rs.getDouble("amount"),
                PaymentStatus.valueOf(rs.getString("status"))
        );
    }
}
