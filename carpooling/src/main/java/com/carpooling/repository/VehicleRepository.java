package com.carpooling.repository;

import com.carpooling.model.Vehicle;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleRepository {

    public void save(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicles (user_id, model, brand, license_plate, seats) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getDriverId());
            ps.setString(2, v.getModel());
            ps.setString(3, v.getBrand());
            ps.setString(4, v.getPlate());
            ps.setInt(5, v.getSeats());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) v.setId(keys.getInt(1));
            }
        }
    }

    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE vehicles SET model=?, brand=?, license_plate=?, seats=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, v.getModel());
            ps.setString(2, v.getBrand());
            ps.setString(3, v.getPlate());
            ps.setInt(4, v.getSeats());
            ps.setInt(5, v.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Vehicle> findByDriver(int driverId) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = """
            SELECT v.*, u.name AS driver_name
            FROM vehicles v JOIN users u ON v.user_id = u.id
            WHERE v.user_id = ?
            ORDER BY v.model
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Optional<Vehicle> findById(int id) throws SQLException {
        String sql = """
            SELECT v.*, u.name AS driver_name
            FROM vehicles v JOIN users u ON v.user_id = u.id
            WHERE v.id = ?
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public boolean isPlateInUse(String plate, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE license_plate=? AND id != ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("model"),
                rs.getString("license_plate"),
                rs.getInt("seats")
        );
        v.setBrand(rs.getString("brand"));
        try { v.setDriverName(rs.getString("driver_name")); } catch (SQLException ignored) {}
        return v;
    }
}
