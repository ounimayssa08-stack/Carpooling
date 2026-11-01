package com.carpooling.service;

import com.carpooling.enums.TripStatus;
import com.carpooling.model.Reservation;
import com.carpooling.model.Trip;
import com.carpooling.repository.ReservationRepository;
import com.carpooling.repository.TripRepository;
import com.carpooling.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class TripService {

    private final TripRepository tripRepo = new TripRepository();
    private final ReservationRepository reservationRepo = new ReservationRepository();
    private final NotificationService notificationService = new NotificationService();

    public Trip createTrip(int driverId, int vehicleId, String departure, String destination,
                           LocalDateTime tripDate, int seats, double price) throws SQLException {
        if (ValidationUtil.isNullOrBlank(departure))
            throw new IllegalArgumentException("Departure city is required.");
        if (ValidationUtil.isNullOrBlank(destination))
            throw new IllegalArgumentException("Destination city is required.");
        if (departure.equalsIgnoreCase(destination))
            throw new IllegalArgumentException("Departure and destination cannot be the same.");
        if (tripDate == null || tripDate.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Trip date must be in the future.");
        if (!ValidationUtil.isPositiveInt(seats))
            throw new IllegalArgumentException("Seats must be a positive number.");
        if (!ValidationUtil.isNonNegativeDouble(price))
            throw new IllegalArgumentException("Price cannot be negative.");

        Trip trip = new Trip(0, driverId, vehicleId, departure, destination,
                tripDate, seats, price, TripStatus.EN_ATTENTE);
        tripRepo.save(trip);
        return trip;
    }

    public void cancelTrip(int tripId, int requestingDriverId) throws SQLException {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));

        if (trip.getDriverId() != requestingDriverId)
            throw new IllegalStateException("You can only cancel your own trips.");
        if (trip.getStatus() == TripStatus.ANNULE)
            throw new IllegalStateException("Trip is already cancelled.");
        if (trip.getStatus() == TripStatus.TERMINE)
            throw new IllegalStateException("Cannot cancel a completed trip.");

        tripRepo.updateStatus(tripId, TripStatus.ANNULE);

        // Notify all confirmed passengers
        List<Reservation> reservations = reservationRepo.findByTrip(tripId);
        for (Reservation r : reservations) {
            if (r.getStatus() == com.carpooling.enums.ReservationStatus.CONFIRMEE) {
                notificationService.sendEmail(r.getUserId(),
                        "Your trip from " + trip.getDeparture() + " to " + trip.getDestination()
                                + " on " + trip.getTripDate().toLocalDate()
                                + " has been cancelled by the driver.");
            }
        }
    }

    public List<Trip> getAllTrips() throws SQLException {
        return Collections.unmodifiableList(tripRepo.findAll());
    }

    public List<Trip> getDriverTrips(int driverId) throws SQLException {
        return Collections.unmodifiableList(tripRepo.findByDriver(driverId));
    }

    public List<Trip> searchTrips(String departure, String destination) throws SQLException {
        String dep = departure == null ? "" : departure.trim();
        String dst = destination == null ? "" : destination.trim();
        return Collections.unmodifiableList(tripRepo.search(dep, dst));
    }

    public Trip getTripById(int id) throws SQLException {
        return tripRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found."));
    }

    /** Called by reservation service after seat change to auto-set FULL status */
    public void refreshTripStatus(int tripId) throws SQLException {
        Trip trip = tripRepo.findById(tripId).orElse(null);
        if (trip == null) return;
        if (trip.getStatus() == TripStatus.ANNULE || trip.getStatus() == TripStatus.TERMINE)
            return;
        if (trip.getAvailableSeats() <= 0) {
            tripRepo.updateStatus(tripId, TripStatus.COMPLET);
        } else if (trip.getStatus() == TripStatus.COMPLET) {
            tripRepo.updateStatus(tripId, TripStatus.EN_ATTENTE);
        }
    }

    public void confirmTrip(int tripId, int driverId) throws SQLException {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable."));
        if (trip.getDriverId() != driverId)
            throw new IllegalStateException("Vous n'êtes pas le conducteur de ce trajet.");
        if (trip.getStatus() != TripStatus.EN_ATTENTE)
            throw new IllegalStateException("Seul un trajet en attente peut être confirmé.");
        
        tripRepo.updateStatus(tripId, TripStatus.CONFIRME);
    }

    public void startTrip(int tripId, int driverId) throws SQLException {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable."));
        if (trip.getDriverId() != driverId)
            throw new IllegalStateException("Vous n'êtes pas le conducteur de ce trajet.");
        if (trip.getStatus() != TripStatus.CONFIRME && trip.getStatus() != TripStatus.EN_ATTENTE)
            throw new IllegalStateException("Le trajet doit être confirmé ou en attente pour démarrer.");
        
        tripRepo.updateStatus(tripId, TripStatus.EN_COURS);

        // Notify passengers
        List<Reservation> resList = reservationRepo.findByTrip(tripId);
        for (Reservation r : resList) {
            if (r.getStatus() == com.carpooling.enums.ReservationStatus.CONFIRMEE) {
                notificationService.sendNotification(r.getUserId(),
                        "Votre trajet de " + trip.getDeparture() + " vers " + trip.getDestination() + " a DÉMARRÉ !", "TRIP");
            }
        }
    }

    public void endTrip(int tripId, int driverId) throws SQLException {
        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable."));
        if (trip.getDriverId() != driverId)
            throw new IllegalStateException("Vous n'êtes pas le conducteur de ce trajet.");
        if (trip.getStatus() != TripStatus.EN_COURS)
            throw new IllegalStateException("Le trajet n'est pas en cours.");
        
        tripRepo.updateStatus(tripId, TripStatus.TERMINE);

        // Notify passengers
        List<Reservation> resList = reservationRepo.findByTrip(tripId);
        for (Reservation r : resList) {
            if (r.getStatus() == com.carpooling.enums.ReservationStatus.CONFIRMEE) {
                notificationService.sendNotification(r.getUserId(),
                        "Votre trajet est ARRIVÉ à destination ! N'oubliez pas de laisser un avis.", "TRIP");
            }
        }
    }
}
