package com.carpooling.util;

import com.carpooling.enums.Role;
import com.carpooling.enums.UserStatus;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite connection and schema initialisation.
 * Auto-creates the database file and all tables on first run.
 * Seeds an admin account if none exists.
 */
public final class DatabaseManager {

    private static final String DB_DIR =
            System.getProperty("user.home") + File.separator + ".carpooling";
    private static final String DB_URL =
            "jdbc:sqlite:" + DB_DIR + File.separator + "carpooling.db";

    private static Connection connection;

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            new File(DB_DIR).mkdirs();
            connection = DriverManager.getConnection(DB_URL);
            // Enable WAL for better concurrency
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA foreign_keys=ON");
            }
        }
        return connection;
    }

    public static void initialize() throws SQLException {
        createTables();
        runMigrations();
        seedAdmin();
    }

    private static void runMigrations() throws SQLException {
        try (Statement st = getConnection().createStatement()) {
            // 1. Payment Methods
            try { st.execute("ALTER TABLE payment_methods ADD COLUMN card_number TEXT NOT NULL DEFAULT ''"); } catch (Exception ignored) {}
            try { st.execute("ALTER TABLE payment_methods ADD COLUMN cvv TEXT NOT NULL DEFAULT ''"); } catch (Exception ignored) {}
            
            // 2. Reservations status & cleanup
            try { st.execute("ALTER TABLE reservations ADD COLUMN status TEXT NOT NULL DEFAULT 'EN_ATTENTE'"); } catch (Exception ignored) {}
            try { st.execute("UPDATE reservations SET status = 'CONFIRMEE' WHERE status = 'CONFIRME'"); } catch (Exception ignored) {}
            
            // 3. Vehicles license_plate, brand, and user_id (formerly driver_id)
            try { st.execute("ALTER TABLE vehicles ADD COLUMN license_plate TEXT"); } catch (Exception ignored) {}
            try { st.execute("ALTER TABLE vehicles ADD COLUMN brand TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { st.execute("ALTER TABLE vehicles ADD COLUMN seats INTEGER DEFAULT 4"); } catch (Exception ignored) {}
            try { st.execute("ALTER TABLE vehicles ADD COLUMN user_id INTEGER"); } catch (Exception ignored) {}
            
            // Data copy for vehicles
            try { st.execute("UPDATE vehicles SET license_plate = plate WHERE license_plate IS NULL"); } catch (Exception ignored) {}
            try { st.execute("UPDATE vehicles SET user_id = driver_id WHERE user_id IS NULL"); } catch (Exception ignored) {}
        }
    }

    private static void createTables() throws SQLException {
        String[] ddl = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                name           TEXT    NOT NULL,
                email          TEXT    NOT NULL UNIQUE,
                password       TEXT    NOT NULL,
                role           TEXT    NOT NULL,
                status         TEXT    NOT NULL DEFAULT 'ACTIVE',
                failed_attempts INTEGER NOT NULL DEFAULT 0
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS vehicles (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id  INTEGER NOT NULL REFERENCES users(id),
                model    TEXT    NOT NULL,
                brand    TEXT    NOT NULL,
                license_plate TEXT NOT NULL UNIQUE,
                seats    INTEGER NOT NULL DEFAULT 4
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS trips (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                driver_id       INTEGER NOT NULL REFERENCES users(id),
                vehicle_id      INTEGER NOT NULL REFERENCES vehicles(id),
                departure       TEXT    NOT NULL,
                destination     TEXT    NOT NULL,
                trip_date       TEXT    NOT NULL,
                available_seats INTEGER NOT NULL,
                price           REAL    NOT NULL DEFAULT 0,
                status          TEXT    NOT NULL DEFAULT 'EN_ATTENTE'
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS reservations (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id        INTEGER NOT NULL REFERENCES users(id),
                trip_id        INTEGER NOT NULL REFERENCES trips(id),
                seats_reserved INTEGER NOT NULL,
                status         TEXT    NOT NULL DEFAULT 'EN_ATTENTE'
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS payments (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                reservation_id INTEGER NOT NULL REFERENCES reservations(id),
                amount         REAL    NOT NULL,
                status         TEXT    NOT NULL DEFAULT 'AUTHORIZED'
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS payment_methods (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id        INTEGER NOT NULL REFERENCES users(id),
                card_number    TEXT    NOT NULL,
                expiry_date    TEXT    NOT NULL,
                cvv            TEXT    NOT NULL,
                card_last_four TEXT    NOT NULL,
                created_at     TEXT    NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS reviews (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                driver_id  INTEGER NOT NULL REFERENCES users(id),
                user_id    INTEGER NOT NULL REFERENCES users(id),
                trip_id    INTEGER NOT NULL REFERENCES trips(id),
                rating     INTEGER NOT NULL,
                comment    TEXT,
                created_at TEXT    NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS notifications (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id  INTEGER NOT NULL REFERENCES users(id),
                message  TEXT    NOT NULL,
                type     TEXT    NOT NULL,
                sent_at  TEXT    NOT NULL,
                is_read  INTEGER NOT NULL DEFAULT 0
            )
            """
        };

        try (Statement st = getConnection().createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
        }
    }

    private static void seedAdmin() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (PreparedStatement ps = getConnection().prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                    INSERT INTO users (name, email, password, role, status, failed_attempts)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement ins = getConnection().prepareStatement(insertSql)) {
                    ins.setString(1, "Admin");
                    ins.setString(2, "admin@carpool.com");
                    ins.setString(3, PasswordUtil.hash("Admin123"));
                    ins.setString(4, Role.ADMIN.name());
                    ins.setString(5, UserStatus.ACTIVE.name());
                    ins.setInt(6, 0);
                    ins.executeUpdate();
                }
            }
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }
}
