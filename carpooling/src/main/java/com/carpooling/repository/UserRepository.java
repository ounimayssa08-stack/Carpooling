package com.carpooling.repository;

import com.carpooling.enums.Role;
import com.carpooling.enums.UserStatus;
import com.carpooling.model.User;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY name";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void save(User user) throws SQLException {
        String sql = """
            INSERT INTO users (name, email, password, role, status, failed_attempts)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getStatus().name());
            ps.setInt(6, user.getFailedAttempts());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        }
    }

    public void update(User user) throws SQLException {
        String sql = """
            UPDATE users SET name=?, email=?, password=?, role=?, status=?, failed_attempts=?
            WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getStatus().name());
            ps.setInt(6, user.getFailedAttempts());
            ps.setInt(7, user.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int userId, UserStatus status) throws SQLException {
        String sql = "UPDATE users SET status=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void incrementFailedAttempts(int userId) throws SQLException {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void resetFailedAttempts(int userId) throws SQLException {
        String sql = "UPDATE users SET failed_attempts=0 WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role")),
                UserStatus.valueOf(rs.getString("status")),
                rs.getInt("failed_attempts")
        );
    }
}
