package com.example.healthmonitor.utils;

import java.util.regex.Pattern;

public class ValidationUtils {
    // Regex kiểm tra Email
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    // Regex kiểm tra mật khẩu (ít nhất 6 ký tự, có chữ và số)
    public static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z]).{6,}$";

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isValidFullName(String fullName) {
        if (isEmpty(fullName)) return false;
        // Loại bỏ khoảng trắng và đếm độ dài
        String cleanName = fullName.replace(" ", "");
        return cleanName.length() >= 6;
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (isEmpty(password)) return false;
        return Pattern.compile(PASSWORD_PATTERN).matcher(password).matches();
    }
}