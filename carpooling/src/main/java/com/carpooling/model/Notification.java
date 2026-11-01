package com.carpooling.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private String type;   // EMAIL | SMS
    private LocalDateTime sentAt;
    private boolean read;

    public Notification() {}

    public Notification(int id, int userId, String message, String type,
                        LocalDateTime sentAt, boolean read) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.sentAt = sentAt;
        this.read = read;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification n)) return false;
        return id == n.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{id=" + id + ", type='" + type
                + "', read=" + read + ", message='" + message + "'}";
    }
}
