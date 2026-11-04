package com.example.healthmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.ui.base.BaseActivity;
import com.example.healthmonitor.utils.PreferenceManager;

public class VerifyCodeActivity extends BaseActivity {

    private EditText[] otpFields;
    private User pendingUser;
    private String password;
    private String sentOtpCode;
    private FirebaseHelper firebaseHelper;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        firebaseHelper = FirebaseHelper.getInstance();
        prefManager = new PreferenceManager(this);

        // Lấy dữ liệu từ Intent
        pendingUser = (User) getIntent().getSerializableExtra("PENDING_USER");
        password = getIntent().getStringExtra("PASSWORD");
        sentOtpCode = getIntent().getStringExtra("OTP_CODE");

        otpFields = new EditText[]{
                findViewById(R.id.etOtp1), findViewById(R.id.etOtp2),
                findViewById(R.id.etOtp3), findViewById(R.id.etOtp4),
                findViewById(R.id.etOtp5), findViewById(R.id.etOtp6)
        };

        setupOtpInputs();

        Button btnVerify = findViewById(R.id.btnVerifyCode);
        btnVerify.setOnClickListener(v -> {
            StringBuilder enteredCode = new StringBuilder();
            for (EditText field : otpFields) {
                enteredCode.append(field.getText().toString());
            }

            if (enteredCode.length() == 6) {
                // Kiểm tra xem đây có phải luồng Forgot Password không
                boolean isForgotPassword = getIntent().getBooleanExtra("IS_FORGOT_PASSWORD", false);
                if (isForgotPassword) {
                    long sentTimestamp = getIntent().getLongExtra("TIMESTAMP", 0);
                    // Giới hạn thời gian mã OTP là 2 phút (120000 ms)
                    if (System.currentTimeMillis() - sentTimestamp > 120000) {
                        Toast.makeText(this, "Verification code has expired. Please try again.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    
                    if (enteredCode.toString().equals(sentOtpCode)) {
                        String resetUserId = getIntent().getStringExtra("USER_ID");
                        Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("USER_ID", resetUserId);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Incorrect verification code", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (enteredCode.toString().equals(sentOtpCode)) {
                        registerUserToDatabase();
                    } else {
                        Toast.makeText(this, "Incorrect verification code", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Please enter all 6 digits", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUserToDatabase() {
        firebaseHelper.registerUser(pendingUser, password, new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(VerifyCodeActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                
                // Lưu trạng thái đăng nhập và userId
                prefManager.setLoginStatus(true);
                prefManager.saveUserId(pendingUser.getUserId());
                prefManager.saveLoginCredentials(pendingUser.getEmail(), password, true);

                // Chuyển đến màn hình nhập thông tin cá nhân với flag IS_FIRST_TIME
                Intent intent = new Intent(VerifyCodeActivity.this, PersonalInfoActivity.class);
                intent.putExtra("IS_FIRST_TIME", true);
                intent.putExtra("USER_DATA", pendingUser);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(VerifyCodeActivity.this, "Error saving data: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }
}
