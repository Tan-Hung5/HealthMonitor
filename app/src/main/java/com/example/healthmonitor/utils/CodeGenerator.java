package com.example.healthmonitor.utils;

import java.util.Random;

public class CodeGenerator {
    /**
     * Tạo mã OTP ngẫu nhiên gồm 6 chữ số.
     */
    public static String generateOTP() {
        Random random = new Random();
        int number = random.nextInt(999999);
        return String.format("%06d", number);
    }
}
