package com.carpooling.service;

import com.carpooling.enums.TripStatus;
import com.carpooling.model.Review;
import com.carpooling.model.Trip;
import com.carpooling.repository.ReservationRepository;
import com.carpooling.repository.ReviewRepository;
import com.carpooling.repository.TripRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ReviewService {

    private final ReviewRepository reviewRepo = new ReviewRepository();
    private final TripRepository tripRepo = new TripRepository();
    private final ReservationRepository reservationRepo = new ReservationRepository();

    public void submitReview(int userId, int tripId, int rating, String comment) throws SQLException {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5.");
        }

        Trip trip = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trajet introuvable."));

        // 1. Check trip is TERMINE
        if (trip.getStatus() != TripStatus.TERMINE) {
            throw new IllegalStateException("Vous ne pouvez évaluer que les trajets terminés.");
        }

        // 2. Check user is passenger
        if (!reservationRepo.isPassenger(userId, tripId)) {
            throw new IllegalStateException("Seuls les passagers ayant effectué le trajet peuvent laisser un avis.");
        }

        // 3. Check user has not already reviewed
        if (reviewRepo.hasReviewed(userId, tripId)) {
            throw new IllegalStateException("Vous avez déjà laissé un avis pour ce trajet.");
        }

        Review review = new Review(0, trip.getDriverId(), userId, tripId, rating, comment, LocalDateTime.now());
        reviewRepo.save(review);
    }

    public double[] getDriverStats(int driverId) throws SQLException {
        return reviewRepo.getAverageRatingAndCount(driverId);
    }
}
