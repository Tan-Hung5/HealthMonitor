package com.example.healthmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.ui.base.BaseActivity;
import com.example.healthmonitor.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends BaseActivity {

    private TextInputLayout tilNewPassword, tilConfirmPassword;
    private TextInputEditText etNewPassword, etConfirmPassword;
    private String userId;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        firebaseHelper = FirebaseHelper.getInstance();
        userId = getIntent().getStringExtra("USER_ID");

        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnResetPassword = findViewById(R.id.btnResetPassword);

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);

            if (!ValidationUtils.isValidPassword(newPassword)) {
                tilNewPassword.setError("Password must be at least 6 characters, including numbers and letters");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Passwords do not match");
                return;
            }

            performResetPassword(newPassword);
        });
    }

    private void performResetPassword(String newPassword) {
        firebaseHelper.resetPassword(userId, newPassword, new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ResetPasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(ResetPasswordActivity.this, "Failed to update password: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}