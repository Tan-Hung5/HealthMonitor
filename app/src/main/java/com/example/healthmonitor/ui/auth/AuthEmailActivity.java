package com.example.healthmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.healthmonitor.R;
import com.example.healthmonitor.ui.base.BaseActivity;

public class AuthEmailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_email);

        Button btnGoToVerifyCode = findViewById(R.id.btnGoToVerifyCode);
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnGoToVerifyCode.setOnClickListener(v -> {
            startActivity(new Intent(AuthEmailActivity.this, VerifyCodeActivity.class));
        });

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AuthEmailActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}