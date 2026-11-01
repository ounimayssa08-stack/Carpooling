package com.carpooling.model;

import com.carpooling.enums.TripStatus;
import java.time.LocalDateTime;
import java.util.Objects;

public class Trip {
    private int id;
    private int driverId;
    private int vehicleId;
    private String departure;
    private String destination;
    private LocalDateTime tripDate;
    private int availableSeats;
    private double price;
    private TripStatus status;

    // denormalized for display
    private String driverName;
    private String vehicleModel;
    private String vehiclePlate;

    public Trip() {}

    public Trip(int id, int driverId, int vehicleId, String departure, String destination,
                LocalDateTime tripDate, int availableSeats, double price, TripStatus status) {
        this.id = id;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.departure = departure;
        this.destination = destination;
        this.tripDate = tripDate;
        this.availableSeats = availableSeats;
        this.price = price;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDateTime getTripDate() { return tripDate; }
    public void setTripDate(LocalDateTime tripDate) { this.tripDate = tripDate; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip t)) return false;
        return id == t.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Trip{id=" + id + ", departure='" + departure + "', destination='"
                + destination + "', date=" + tripDate + ", status=" + status + "}";
    }
}
