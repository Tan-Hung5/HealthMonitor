package com.example.healthmonitor.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.ui.base.BaseActivity;
import com.example.healthmonitor.utils.CodeGenerator;
import com.example.healthmonitor.utils.EmailUtils;
import com.example.healthmonitor.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends BaseActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private ProgressDialog progressDialog;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        firebaseHelper = FirebaseHelper.getInstance();

        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        Button btnSendOtp = findViewById(R.id.btnSendOtp);
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking email and sending code...");
        progressDialog.setCancelable(false);

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!ValidationUtils.isValidEmail(email)) {
                tilEmail.setError("Invalid email address");
                return;
            }
            tilEmail.setError(null);
            checkEmailAndSendCode(email);
        });

        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void checkEmailAndSendCode(String email) {
        progressDialog.show();
        firebaseHelper.checkEmailExists(email, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Email hợp lệ và tồn tại, tiến hành tạo và gửi mã OTP
                String otpCode = CodeGenerator.generateOTP();
                String subject = "Health Monitor Password Reset Code";
                String body = "You requested to reset your password.\n\nYour verification code is: " + otpCode + "\n\nPlease enter this code to reset your password. The code will expire shortly.";

                EmailUtils.sendEmailAuto(email, subject, body, new EmailUtils.EmailCallback() {
                    @Override
                    public void onSendSuccess() {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Verification code sent successfully!", Toast.LENGTH_SHORT).show();

                        // Chuyển sang VerifyCodeActivity nhưng với cờ cho reset password
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
                        intent.putExtra("IS_FORGOT_PASSWORD", true);
                        intent.putExtra("USER_ID", user.getUserId());
                        intent.putExtra("OTP_CODE", otpCode);
                        intent.putExtra("TIMESTAMP", System.currentTimeMillis()); // Phục vụ kiểm tra giới hạn thời gian (e.g. 2 phút)
                        startActivity(intent);
                    }

                    @Override
                    public void onSendFailure(String error) {
                        progressDialog.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "Failed to send code: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                progressDialog.dismiss();
                tilEmail.setError("Email does not exist in our database");
                Toast.makeText(ForgotPasswordActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}