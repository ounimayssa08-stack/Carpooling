package com.carpooling.util;

import java.util.regex.Pattern;

/**
 * Input validation helpers used by service layer.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");

    private ValidationUtil() {}

    public static boolean isNullOrBlank(String s) {
        return s == null || s.isBlank();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        // Minimum 6 characters
        return password != null && password.length() >= 6;
    }

    public static boolean isPositiveInt(int value) {
        return value > 0;
    }

    public static boolean isNonNegativeDouble(double value) {
        return value >= 0;
    }
}
