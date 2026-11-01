package com.carpooling.model;

import java.util.Objects;

public class Vehicle {
    private int id;
    private int driverId;
    private String model;
    private String brand;
    private String plate;
    private int seats;
    // denormalized for display
    private String driverName;

    public Vehicle() {}

    public Vehicle(int id, int driverId, String model, String plate, int seats) {
        this.id = id;
        this.driverId = driverId;
        this.model = model;
        this.plate = plate;
        this.seats = seats;
    }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle v)) return false;
        return id == v.id && Objects.equals(plate, v.plate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, plate);
    }

    @Override
    public String toString() {
        return "Vehicle{id=" + id + ", model='" + model + "', plate='" + plate
                + "', seats=" + seats + "}";
    }
}
