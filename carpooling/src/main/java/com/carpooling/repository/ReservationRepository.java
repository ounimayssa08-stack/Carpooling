package com.carpooling.repository;

import com.carpooling.enums.ReservationStatus;
import com.carpooling.model.Reservation;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationRepository {

    public void save(Reservation r) throws SQLException {
        String sql = """
            INSERT INTO reservations (user_id, trip_id, seats_reserved, status)
            VALUES (?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getUserId());
            ps.setInt(2, r.getTripId());
            ps.setInt(3, r.getSeatsReserved());
            ps.setString(4, r.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getInt(1));
            }
        }
    }

    public void updateStatus(int reservationId, ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET status=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }

    public Optional<Reservation> findById(int id) throws SQLException {
        String sql = buildSelect("r.id=?");
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Reservation> findByUser(int userId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = buildSelect("r.user_id=?") + " ORDER BY t.trip_date DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Reservation> findByTrip(int tripId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = buildSelect("r.trip_id=?");
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countConfirmedSeats(int tripId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(seats_reserved), 0)
            FROM reservations
            WHERE trip_id=? AND status='CONFIRMEE'
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean hasActiveReservation(int userId, int tripId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE user_id = ? AND trip_id = ? AND status IN ('EN_ATTENTE', 'CONFIRMEE')";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isPassenger(int userId, int tripId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE user_id = ? AND trip_id = ? AND status = 'CONFIRMEE'";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String buildSelect(String where) {
        return """
            SELECT r.*,
                   t.departure, t.destination, t.trip_date, t.price AS price_per_seat,
                   u.name AS driver_name,
                   p.name AS passenger_name
            FROM reservations r
            JOIN trips t ON r.trip_id = t.id
            JOIN users u ON t.driver_id = u.id
            JOIN users p ON r.user_id = p.id
            WHERE\s""" + where;
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("trip_id"),
                rs.getInt("seats_reserved"),
                rs.getString("status") != null ? parseStatus(rs.getString("status")) : ReservationStatus.EN_ATTENTE
        );
        r.setDeparture(rs.getString("departure"));
        r.setDestination(rs.getString("destination"));
        r.setTripDate(rs.getString("trip_date"));
        r.setPricePerSeat(rs.getDouble("price_per_seat"));
        r.setDriverName(rs.getString("driver_name"));
        r.setPassengerName(rs.getString("passenger_name"));
        return r;
    }

    private ReservationStatus parseStatus(String s) {
        if ("CONFIRME".equals(s)) return ReservationStatus.CONFIRMEE;
        try {
            return ReservationStatus.valueOf(s);
        } catch (Exception e) {
            return ReservationStatus.EN_ATTENTE;
        }
    }
}
