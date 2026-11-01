package com.carpooling.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Review {
    private int id;
    private int driverId;
    private int userId;
    private int tripId;
    private int rating;        // 1-5
    private String comment;
    private LocalDateTime createdAt;

    // denormalized for display
    private String userName;
    private String driverName;

    public Review() {}

    public Review(int id, int driverId, int userId, int tripId,
                  int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.driverId = driverId;
        this.userId = userId;
        this.tripId = tripId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    /** Returns a star string like ★★★★☆ */
    public String getStars() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review r)) return false;
        return id == r.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Review{id=" + id + ", driver=" + driverId
                + ", user=" + userId + ", rating=" + rating + "}";
    }
}
