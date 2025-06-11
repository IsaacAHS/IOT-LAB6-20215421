package com.example.telegest;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return email != null && !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }

    public static boolean isValidAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return false;
        }

        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}