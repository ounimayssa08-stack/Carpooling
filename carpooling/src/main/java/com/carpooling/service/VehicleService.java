package com.carpooling.service;

import com.carpooling.model.Vehicle;
import com.carpooling.repository.VehicleRepository;
import com.carpooling.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class VehicleService {

    private final VehicleRepository vehicleRepo = new VehicleRepository();

    public Vehicle addVehicle(int driverId, String model, String brand, String plate, int seats)
            throws SQLException {
        validate(model, plate, seats);
        if (vehicleRepo.isPlateInUse(plate, -1))
            throw new IllegalArgumentException("Plate number already registered.");

        Vehicle v = new Vehicle(0, driverId, model, plate, seats);
        v.setBrand(brand);
        vehicleRepo.save(v);
        return v;
    }

    public void updateVehicle(Vehicle v, String model, String brand, String plate, int seats)
            throws SQLException {
        validate(model, plate, seats);
        if (vehicleRepo.isPlateInUse(plate, v.getId()))
            throw new IllegalArgumentException("Plate number already used by another vehicle.");

        v.setModel(model);
        v.setBrand(brand);
        v.setPlate(plate);
        v.setSeats(seats);
        vehicleRepo.update(v);
    }

    public void deleteVehicle(int vehicleId) throws SQLException {
        vehicleRepo.delete(vehicleId);
    }

    public List<Vehicle> getDriverVehicles(int driverId) throws SQLException {
        return Collections.unmodifiableList(vehicleRepo.findByDriver(driverId));
    }

    private void validate(String model, String plate, int seats) {
        if (ValidationUtil.isNullOrBlank(model))
            throw new IllegalArgumentException("Vehicle model is required.");
        if (ValidationUtil.isNullOrBlank(plate))
            throw new IllegalArgumentException("Plate number is required.");
        if (!ValidationUtil.isPositiveInt(seats))
            throw new IllegalArgumentException("Seats must be a positive number.");
    }
}
