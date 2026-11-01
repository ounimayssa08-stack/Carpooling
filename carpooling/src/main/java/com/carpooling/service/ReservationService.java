package com.carpooling.service;

import com.carpooling.enums.PaymentStatus;
import com.carpooling.enums.ReservationStatus;
import com.carpooling.enums.TripStatus;
import com.carpooling.model.Payment;
import com.carpooling.model.Reservation;
import com.carpooling.model.Trip;
import com.carpooling.repository.PaymentRepository;
import com.carpooling.repository.ReservationRepository;
import com.carpooling.repository.TripRepository;
import com.carpooling.repository.PaymentMethodRepository;
import com.carpooling.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class ReservationService {

    private final ReservationRepository reservationRepo = new ReservationRepository();
    private final TripRepository tripRepo = new TripRepository();
    private final PaymentRepository paymentRepo = new PaymentRepository();
    private final TripService tripService = new TripService();
    private final PaymentService paymentService = new PaymentService();
    private final NotificationService notificationService = new NotificationService();

    private final PaymentMethodRepository paymentMethodRepo = new PaymentMethodRepository();

    public Reservation reserve(int userId, int tripId, int seats) throws SQLException {
        if (!ValidationUtil.isPositiveInt(seats))
            throw new IllegalArgumentException("Les places doivent être un nombre positif.");

        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable."));

        if (trip.getStatus() == TripStatus.ANNULE)
            throw new IllegalStateException("Ce trajet a été annulé.");
        if (trip.getStatus() == TripStatus.TERMINE || trip.getStatus() == TripStatus.EN_COURS)
            throw new IllegalStateException("Impossible de réserver un trajet déjà en cours ou terminé.");
        if (trip.getAvailableSeats() < seats)
            throw new IllegalStateException("Pas assez de places disponibles. Seulement "
                    + trip.getAvailableSeats() + " place(s) restante(s).");
        if (trip.getDriverId() == userId)
            throw new IllegalStateException("Les conducteurs ne peuvent pas réserver leurs propres trajets.");
        if (reservationRepo.hasActiveReservation(userId, tripId))
            throw new IllegalStateException("Vous avez déjà une réservation active pour ce trajet.");

        if (!paymentMethodRepo.hasPaymentMethod(userId))
            throw new IllegalStateException("Veuillez ajouter un moyen de paiement (Moyens de paiement) avant de réserver.");

        // Save reservation as pending
        Reservation r = new Reservation(0, userId, tripId, seats, ReservationStatus.EN_ATTENTE);
        reservationRepo.save(r);

        // Notify driver
        notificationService.sendEmail(trip.getDriverId(),
                "Nouvelle demande de réservation ! " + seats + " place(s) pour le trajet de "
                        + trip.getDeparture() + " à " + trip.getDestination() + ".");

        return r;
    }

    public void acceptReservation(int reservationId, int driverId) throws SQLException {
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable."));
        Trip trip = tripRepo.findById(r.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable."));
        
        if (trip.getDriverId() != driverId)
            throw new IllegalStateException("Vous n'êtes pas le conducteur de ce trajet.");
        if (r.getStatus() != ReservationStatus.EN_ATTENTE)
            throw new IllegalStateException("Cette réservation n'est plus en attente.");
        if (trip.getAvailableSeats() < r.getSeatsReserved()) {
            reservationRepo.updateStatus(reservationId, ReservationStatus.REFUSEE);
            throw new IllegalStateException("Pas assez de places pour accepter cette réservation.");
        }

        reservationRepo.updateStatus(reservationId, ReservationStatus.CONFIRMEE);

        // Deduct seats
        int newSeats = trip.getAvailableSeats() - r.getSeatsReserved();
        tripRepo.updateAvailableSeats(trip.getId(), newSeats);
        tripService.refreshTripStatus(trip.getId());

        // Create authorized payment
        double amount = r.getSeatsReserved() * trip.getPrice();
        paymentService.authorizePayment(r.getId(), amount);

        // Notify passenger
        notificationService.sendNotification(r.getUserId(),
                "Votre réservation a été CONFIRMÉE ! Trajet: "
                        + trip.getDeparture() + " → " + trip.getDestination(), "RESERVATION");
    }

    public void refuseReservation(int reservationId, int driverId) throws SQLException {
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable."));
        Trip trip = tripRepo.findById(r.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable."));
        
        if (trip.getDriverId() != driverId)
            throw new IllegalStateException("Vous n'êtes pas le conducteur de ce trajet.");
        if (r.getStatus() != ReservationStatus.EN_ATTENTE)
            throw new IllegalStateException("Cette réservation n'est plus en attente.");

        reservationRepo.updateStatus(reservationId, ReservationStatus.REFUSEE);

        // Notify passenger
        notificationService.sendNotification(r.getUserId(),
                "Votre réservation a été REFUSÉE pour le trajet: "
                        + trip.getDeparture() + " → " + trip.getDestination(), "RESERVATION");
    }

    public void cancelReservation(int reservationId, int userId) throws SQLException {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        if (reservation.getUserId() != userId)
            throw new IllegalStateException("You can only cancel your own reservations.");
        if (reservation.getStatus() == ReservationStatus.ANNULEE)
            throw new IllegalStateException("Reservation is already cancelled.");

        Trip trip = tripRepo.findById(reservation.getTripId())
                .orElseThrow(() -> new IllegalArgumentException("Associated trip not found."));

        if (trip.getStatus() == TripStatus.TERMINE)
            throw new IllegalStateException("Cannot cancel reservation for a completed trip.");

        reservationRepo.updateStatus(reservationId, ReservationStatus.ANNULEE);

        // Restore seats
        int restoredSeats = trip.getAvailableSeats() + reservation.getSeatsReserved();
        tripRepo.updateAvailableSeats(trip.getId(), restoredSeats);
        tripService.refreshTripStatus(trip.getId());

        // Process refund
        long hoursUntilTrip = ChronoUnit.HOURS.between(LocalDateTime.now(), trip.getTripDate());
        paymentService.processRefund(reservationId, hoursUntilTrip);

        // Notify
        notificationService.sendEmail(userId,
                "Reservation cancelled for trip " + trip.getDeparture()
                        + " → " + trip.getDestination() + ". "
                        + (hoursUntilTrip > 24 ? "Full refund issued." : "Partial refund (50%) issued."));
        notificationService.sendSMS(userId,
                "Reservation cancelled: " + trip.getDeparture() + " → " + trip.getDestination());
    }

    public boolean hasActiveReservation(int userId, int tripId) throws SQLException {
        return reservationRepo.hasActiveReservation(userId, tripId);
    }

    public List<Reservation> getUserReservations(int userId) throws SQLException {
        return Collections.unmodifiableList(reservationRepo.findByUser(userId));
    }

    public List<Reservation> getTripReservations(int tripId) throws SQLException {
        return Collections.unmodifiableList(reservationRepo.findByTrip(tripId));
    }
}
