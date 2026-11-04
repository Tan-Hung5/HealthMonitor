package com.example.healthmonitor.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtils {

    // Cấu hình tài khoản gửi (Bắt buộc dùng App Password của Google)
    // Bạn cần bật xác thực 2 lớp và tạo App Password tại: https://myaccount.google.com/apppasswords
    private static final String SENDER_EMAIL = "healthmonitor.company@gmail.com";
    private static final String SENDER_PASSWORD = "kumu obyy xbks lwcr"; // Thay bằng App Password 16 ký tự của bạn

    public interface EmailCallback {
        void onSendSuccess();
        void onSendFailure(String error);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Gửi email tự động ngầm không cần người dùng can thiệp qua ứng dụng Gmail.
     */
    public static void sendEmailAuto(String recipient, String subject, String body, EmailCallback callback) {
        executor.execute(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setText(body);
                Transport.send(message);

                handler.post(() -> {
                    if (callback != null) callback.onSendSuccess();
                });

            } catch (MessagingException e) {
                String errorMsg = e.getMessage();
                Log.e("EmailUtils", "Send failed: " + errorMsg);
                handler.post(() -> {
                    if (callback != null) callback.onSendFailure(errorMsg);
                });
            }
        });
    }
}
