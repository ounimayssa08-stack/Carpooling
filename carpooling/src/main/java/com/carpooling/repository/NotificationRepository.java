package com.carpooling.repository;

import com.carpooling.model.Notification;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    public void save(Notification n) throws SQLException {
        String sql = """
            INSERT INTO notifications (user_id, message, type, sent_at, is_read)
            VALUES (?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getMessage());
            ps.setString(3, n.getType());
            ps.setString(4, n.getSentAt().toString());
            ps.setInt(5, n.isRead() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) n.setId(keys.getInt(1));
            }
        }
    }

    public List<Notification> findByUser(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id=? ORDER BY sent_at DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void markAllRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read=1 WHERE user_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        return new Notification(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("message"),
                rs.getString("type"),
                LocalDateTime.parse(rs.getString("sent_at")),
                rs.getInt("is_read") == 1
        );
    }
}
