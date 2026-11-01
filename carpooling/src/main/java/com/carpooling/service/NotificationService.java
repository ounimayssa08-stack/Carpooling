package com.carpooling.service;

import com.carpooling.model.Notification;
import com.carpooling.repository.NotificationRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class NotificationService {

    private final NotificationRepository notificationRepo = new NotificationRepository();

    public void sendEmail(int userId, String message) throws SQLException {
        sendNotification(userId, message, "EMAIL");
    }

    public void sendSMS(int userId, String message) throws SQLException {
        sendNotification(userId, message, "SMS");
    }

    public void sendNotification(int userId, String message, String type) throws SQLException {
        Notification n = new Notification(0, userId, message, type,
                LocalDateTime.now(), false);
        notificationRepo.save(n);
        // Simulate sending
        System.out.println("[" + type + "] To userId=" + userId + ": " + message);
    }

    public List<Notification> getUserNotifications(int userId) throws SQLException {
        return Collections.unmodifiableList(notificationRepo.findByUser(userId));
    }

    public int getUnreadCount(int userId) throws SQLException {
        return notificationRepo.countUnread(userId);
    }

    public void markAllRead(int userId) throws SQLException {
        notificationRepo.markAllRead(userId);
    }
}
