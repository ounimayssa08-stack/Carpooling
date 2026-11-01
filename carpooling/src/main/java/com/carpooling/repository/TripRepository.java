package com.carpooling.repository;

import com.carpooling.enums.TripStatus;
import com.carpooling.model.Trip;
import com.carpooling.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TripRepository {

    public void save(Trip t) throws SQLException {
        String sql = """
            INSERT INTO trips (driver_id, vehicle_id, departure, destination,
                               trip_date, available_seats, price, status)
            VALUES (?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getDriverId());
            ps.setInt(2, t.getVehicleId());
            ps.setString(3, t.getDeparture());
            ps.setString(4, t.getDestination());
            ps.setString(5, t.getTripDate().toString());
            ps.setInt(6, t.getAvailableSeats());
            ps.setDouble(7, t.getPrice());
            ps.setString(8, t.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setId(keys.getInt(1));
            }
        }
    }

    public void update(Trip t) throws SQLException {
        String sql = """
            UPDATE trips SET vehicle_id=?, departure=?, destination=?,
                trip_date=?, available_seats=?, price=?, status=?
            WHERE id=?
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, t.getVehicleId());
            ps.setString(2, t.getDeparture());
            ps.setString(3, t.getDestination());
            ps.setString(4, t.getTripDate().toString());
            ps.setInt(5, t.getAvailableSeats());
            ps.setDouble(6, t.getPrice());
            ps.setString(7, t.getStatus().name());
            ps.setInt(8, t.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(int tripId, TripStatus status) throws SQLException {
        String sql = "UPDATE trips SET status=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, tripId);
            ps.executeUpdate();
        }
    }

    public void updateAvailableSeats(int tripId, int seats) throws SQLException {
        String sql = "UPDATE trips SET available_seats=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, seats);
            ps.setInt(2, tripId);
            ps.executeUpdate();
        }
    }

    public Optional<Trip> findById(int id) throws SQLException {
        String sql = buildSelectQuery("t.id=?");
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Trip> findAll() throws SQLException {
        List<Trip> list = new ArrayList<>();
        String sql = buildSelectQuery("1=1") + " ORDER BY t.trip_date DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Trip> findByDriver(int driverId) throws SQLException {
        List<Trip> list = new ArrayList<>();
        String sql = buildSelectQuery("t.driver_id=?") + " ORDER BY t.trip_date DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Trip> search(String departure, String destination) throws SQLException {
        List<Trip> list = new ArrayList<>();
        String sql = buildSelectQuery(
                "t.status IN ('EN_ATTENTE','COMPLET') AND LOWER(t.departure) LIKE ? AND LOWER(t.destination) LIKE ?")
                + " ORDER BY t.trip_date ASC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + departure.toLowerCase() + "%");
            ps.setString(2, "%" + destination.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private String buildSelectQuery(String where) {
        return """
            SELECT t.*, u.name AS driver_name, v.model AS vehicle_model, v.license_plate AS vehicle_plate
            FROM trips t
            JOIN users u ON t.driver_id = u.id
            JOIN vehicles v ON t.vehicle_id = v.id
            WHERE\s""" + where;
    }

    private Trip map(ResultSet rs) throws SQLException {
        Trip t = new Trip(
                rs.getInt("id"),
                rs.getInt("driver_id"),
                rs.getInt("vehicle_id"),
                rs.getString("departure"),
                rs.getString("destination"),
                LocalDateTime.parse(rs.getString("trip_date")),
                rs.getInt("available_seats"),
                rs.getDouble("price"),
                TripStatus.valueOf(rs.getString("status"))
        );
        t.setDriverName(rs.getString("driver_name"));
        t.setVehicleModel(rs.getString("vehicle_model"));
        t.setVehiclePlate(rs.getString("vehicle_plate"));
        return t;
    }
}
