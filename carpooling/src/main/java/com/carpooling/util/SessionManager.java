package com.carpooling.util;

import com.carpooling.model.User;

/**
 * Holds the currently authenticated user for the session.
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
