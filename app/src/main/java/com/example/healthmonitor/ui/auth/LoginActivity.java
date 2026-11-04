package com.example.healthmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.ui.base.BaseActivity;
import com.example.healthmonitor.ui.main.MainActivity;
import com.example.healthmonitor.utils.PreferenceManager;
import com.example.healthmonitor.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends BaseActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private PreferenceManager prefManager;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefManager = new PreferenceManager(this);
        // Áp dụng theme trước khi setContentView
        applyTheme();
        
        super.onCreate(savedInstanceState);
        
        firebaseHelper = FirebaseHelper.getInstance();
        
        if (prefManager.isLoggedIn()) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        if (prefManager.isRememberMeChecked()) {
            etEmail.setText(prefManager.getSavedEmail());
            etPassword.setText(prefManager.getSavedPassword());
            cbRememberMe.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> {
            if (validateLogin()) {
                performLogin();
            }
        });

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void applyTheme() {
        if (prefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void performLogin() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        boolean rememberMe = cbRememberMe.isChecked();

        firebaseHelper.login(email, password, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                prefManager.setLoginStatus(true);
                prefManager.saveUserId(user.getUserId());
                prefManager.saveLoginCredentials(email, password, rememberMe);
                startMainActivity();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(LoginActivity.this, "Login failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private boolean validateLogin() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        boolean isValid = true;

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Invalid Email address");
            isValid = false;
        }

        if (ValidationUtils.isEmpty(password)) {
            tilPassword.setError("Please enter your password");
            isValid = false;
        }

        return isValid;
    }
}
