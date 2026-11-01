package com.carpooling.model;

import com.carpooling.enums.ReservationStatus;
import java.util.Objects;

public class Reservation {
    private int id;
    private int userId;
    private int tripId;
    private int seatsReserved;
    private ReservationStatus status;

    // denormalized for display
    private String departure;
    private String destination;
    private String tripDate;
    private String driverName;
    private double pricePerSeat;
    private String passengerName;

    public Reservation() {}

    public Reservation(int id, int userId, int tripId,
                       int seatsReserved, ReservationStatus status) {
        this.id = id;
        this.userId = userId;
        this.tripId = tripId;
        this.seatsReserved = seatsReserved;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getSeatsReserved() { return seatsReserved; }
    public void setSeatsReserved(int seatsReserved) { this.seatsReserved = seatsReserved; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTripDate() { return tripDate; }
    public void setTripDate(String tripDate) { this.tripDate = tripDate; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public double getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(double pricePerSeat) { this.pricePerSeat = pricePerSeat; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public double getTotalAmount() {
        return seatsReserved * pricePerSeat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation r)) return false;
        return id == r.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", tripId=" + tripId
                + ", seats=" + seatsReserved + ", status=" + status + "}";
    }
}
