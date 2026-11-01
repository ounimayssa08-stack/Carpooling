package com.carpooling.service;

import com.carpooling.enums.Role;
import com.carpooling.enums.UserStatus;
import com.carpooling.model.User;
import com.carpooling.repository.UserRepository;
import com.carpooling.util.PasswordUtil;
import com.carpooling.util.SessionManager;
import com.carpooling.util.ValidationUtil;

import java.sql.SQLException;

public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private final UserRepository userRepo = new UserRepository();

    /**
     * Registers a new user. Throws IllegalArgumentException for bad input.
     */
    public User register(String name, String email, String password, Role role) throws SQLException {
        if (ValidationUtil.isNullOrBlank(name))
            throw new IllegalArgumentException("Name is required.");
        if (!ValidationUtil.isValidEmail(email))
            throw new IllegalArgumentException("Invalid email address.");
        if (!ValidationUtil.isValidPassword(password))
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (role == null)
            throw new IllegalArgumentException("Role is required.");
        if (userRepo.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Email already registered.");

        User user = new User(0, name, email, PasswordUtil.hash(password),
                role, UserStatus.ACTIVE, 0);
        userRepo.save(user);
        return user;
    }

    /**
     * Authenticates a user. Updates failed attempts and blocks on threshold.
     * @throws IllegalStateException for blocked/suspended accounts
     * @throws IllegalArgumentException for invalid credentials
     */
    public User login(String email, String password) throws SQLException {
        if (ValidationUtil.isNullOrBlank(email) || ValidationUtil.isNullOrBlank(password))
            throw new IllegalArgumentException("Email and password are required.");

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (user.getStatus() == UserStatus.BLOCKED)
            throw new IllegalStateException("Account blocked due to too many failed login attempts.");
        if (user.getStatus() == UserStatus.SUSPENDED)
            throw new IllegalStateException("Account suspended. Please contact an administrator.");

        if (!PasswordUtil.verify(password, user.getPassword())) {
            userRepo.incrementFailedAttempts(user.getId());
            int remaining = MAX_FAILED_ATTEMPTS - (user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() + 1 >= MAX_FAILED_ATTEMPTS) {
                userRepo.updateStatus(user.getId(), UserStatus.BLOCKED);
                throw new IllegalStateException("Too many failed attempts. Account is now blocked.");
            }
            throw new IllegalArgumentException(
                    "Invalid email or password. " + Math.max(0, remaining) + " attempt(s) remaining.");
        }

        userRepo.resetFailedAttempts(user.getId());
        // Reload to get fresh state
        user = userRepo.findByEmail(email).orElseThrow();
        SessionManager.setCurrentUser(user);
        return user;
    }

    public void logout() {
        SessionManager.clear();
    }
}
