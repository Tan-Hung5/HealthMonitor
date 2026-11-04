package com.example.healthmonitor.ui.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.ui.base.BaseActivity;
import com.example.healthmonitor.utils.CodeGenerator;
import com.example.healthmonitor.utils.EmailUtils;
import com.example.healthmonitor.utils.PreferenceManager;
import com.example.healthmonitor.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends BaseActivity {

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;
    private ProgressDialog progressDialog;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefManager = new PreferenceManager(this);
        applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending verification code...");
        progressDialog.setCancelable(false);

        btnRegister.setOnClickListener(v -> {
            if (validateData()) {
                sendVerificationCodeAuto();
            }
        });

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void applyTheme() {
        if (prefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void sendVerificationCodeAuto() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        progressDialog.show();

        // 1. Tạo mã OTP
        String otpCode = CodeGenerator.generateOTP();

        // 2. Nội dung Email
        String subject = "Health Monitor Registration Verification Code";
        String body = "Hello " + fullName + ",\n\nYour verification code is: " + otpCode + "\n\nPlease enter this code in the app to complete registration.";

        // 3. Gửi Email tự động ngầm
        EmailUtils.sendEmailAuto(email, subject, body, new EmailUtils.EmailCallback() {
            @Override
            public void onSendSuccess() {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Verification code sent!", Toast.LENGTH_SHORT).show();

                // 4. Chuyển sang màn hình VerifyCodeActivity
                User pendingUser = new User();
                pendingUser.setName(fullName);
                pendingUser.setEmail(email);
                pendingUser.setBirthdate("");
                pendingUser.setPhonenumber("");

                Intent intent = new Intent(RegisterActivity.this, VerifyCodeActivity.class);
                intent.putExtra("PENDING_USER", pendingUser);
                intent.putExtra("PASSWORD", password);
                intent.putExtra("OTP_CODE", otpCode);
                startActivity(intent);
            }

            @Override
            public void onSendFailure(String error) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Failed to send email: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateData() {
        String fullName = etFullName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        boolean isValid = true;
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        if (!ValidationUtils.isValidFullName(fullName)) {
            tilFullName.setError("Invalid full name");
            isValid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Invalid email address");
            isValid = false;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError("Weak password");
            isValid = false;
        }
        if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        return isValid;
    }
}
