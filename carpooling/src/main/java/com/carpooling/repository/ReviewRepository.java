package com.carpooling.repository;

import com.carpooling.model.Review;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {

    public void save(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (driver_id, user_id, trip_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, review.getDriverId());
            ps.setInt(2, review.getUserId());
            ps.setInt(3, review.getTripId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.setString(6, review.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) review.setId(keys.getInt(1));
            }
        }
    }

    public List<Review> findByDriver(int driverId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String sql = """
            SELECT r.*, u.name AS user_name
            FROM reviews r
            JOIN users u ON r.user_id = u.id
            WHERE r.driver_id = ?
            ORDER BY r.created_at DESC
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review(
                            rs.getInt("id"),
                            rs.getInt("driver_id"),
                            rs.getInt("user_id"),
                            rs.getInt("trip_id"),
                            rs.getInt("rating"),
                            rs.getString("comment"),
                            LocalDateTime.parse(rs.getString("created_at"))
                    );
                    r.setUserName(rs.getString("user_name"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    public boolean hasReviewed(int userId, int tripId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND trip_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public double[] getAverageRatingAndCount(int driverId) throws SQLException {
        String sql = "SELECT AVG(rating), COUNT(*) FROM reviews WHERE driver_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new double[]{rs.getDouble(1), rs.getDouble(2)};
                }
            }
        }
        return new double[]{0.0, 0.0};
    }

    public double getAverageRating(int driverId) throws SQLException {
        return getAverageRatingAndCount(driverId)[0];
    }

    public int getReviewCount(int driverId) throws SQLException {
        return (int) getAverageRatingAndCount(driverId)[1];
    }
}
