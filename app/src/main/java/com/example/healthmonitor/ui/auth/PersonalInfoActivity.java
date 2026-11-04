package com.example.healthmonitor.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.ui.base.BaseActivity;
import com.example.healthmonitor.ui.main.MainActivity;
import com.example.healthmonitor.utils.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PersonalInfoActivity extends BaseActivity {

    private TextInputLayout tilFullName, tilEmail, tilPhoneNumber;
    private TextInputEditText etFullName, etEmail, etPhoneNumber, etBirthdateDisplay;
    private View layoutBirthdateView;
    private LinearLayout layoutBirthdateEdit;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private ImageButton btnEditBirthdate;
    private Button btnSaveInfo;
    
    private User currentUser;
    private FirebaseHelper firebaseHelper;
    private PreferenceManager prefManager;
    private boolean isFirstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefManager = new PreferenceManager(this);
        applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        isFirstTime = getIntent().getBooleanExtra("IS_FIRST_TIME", false);
        firebaseHelper = FirebaseHelper.getInstance();

        // Bind Views
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etBirthdateDisplay = findViewById(R.id.etBirthdateDisplay);
        
        layoutBirthdateView = findViewById(R.id.layout_birthdate_view);
        layoutBirthdateEdit = findViewById(R.id.layout_birthdate_edit);
        
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnEditBirthdate = findViewById(R.id.btnEditBirthdate);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);

        setupSpinners();
        
        if (isFirstTime) {
            setupFirstTimeUI();
        } else {
            setupEditActions();
        }

        loadUserData();

        btnSaveInfo.setOnClickListener(v -> saveInfo());
    }

    private void applyTheme() {
        if (prefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void setupFirstTimeUI() {
        etFullName.setEnabled(true);
        etPhoneNumber.setEnabled(true);
        
        layoutBirthdateView.setVisibility(View.GONE);
        layoutBirthdateEdit.setVisibility(View.VISIBLE);
        
        tilFullName.setEndIconVisible(false);
        tilPhoneNumber.setEndIconVisible(false);
        
        btnSaveInfo.setVisibility(View.VISIBLE);
        btnSaveInfo.setText("Finish");
    }

    private void setupSpinners() {
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) days.add(String.format("%02d", i));
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) months.add(String.format("%02d", i));
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 1940; i--) years.add(String.valueOf(i));
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void setupEditActions() {
        tilFullName.setEndIconOnClickListener(v -> {
            etFullName.setEnabled(true);
            etFullName.requestFocus();
            btnSaveInfo.setVisibility(View.VISIBLE);
        });

        tilPhoneNumber.setEndIconOnClickListener(v -> {
            etPhoneNumber.setEnabled(true);
            etPhoneNumber.requestFocus();
            btnSaveInfo.setVisibility(View.VISIBLE);
        });

        btnEditBirthdate.setOnClickListener(v -> {
            layoutBirthdateView.setVisibility(View.GONE);
            layoutBirthdateEdit.setVisibility(View.VISIBLE);
            btnSaveInfo.setVisibility(View.VISIBLE);
        });
    }

    private void loadUserData() {
        String userId = prefManager.getUserId();
        if (userId.isEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper.fetchUserData(userId, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                if (user != null) {
                    etFullName.setText(user.getName());
                    etEmail.setText(user.getEmail());
                    etPhoneNumber.setText(user.getPhonenumber());
                    
                    String dateStr = user.getBirthdate();
                    etBirthdateDisplay.setText(dateStr != null ? dateStr : "Not updated");
                    
                    if (dateStr != null && dateStr.contains("/")) {
                        String[] parts = dateStr.split("/");
                        if (parts.length == 3) {
                            setSpinnerValue(spinnerDay, parts[0]);
                            setSpinnerValue(spinnerMonth, parts[1]);
                            setSpinnerValue(spinnerYear, parts[2]);
                        }
                    }
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(PersonalInfoActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveInfo() {
        if (currentUser == null) return;

        String name = etFullName.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        
        String day = spinnerDay.getSelectedItem().toString();
        String month = spinnerMonth.getSelectedItem().toString();
        String year = spinnerYear.getSelectedItem().toString();
        String birthdate = day + "/" + month + "/" + year;

        if (name.isEmpty()) {
            tilFullName.setError("Please enter your full name");
            return;
        }

        currentUser.setName(name);
        currentUser.setPhonenumber(phone);
        currentUser.setBirthdate(birthdate);

        firebaseHelper.updateUserProfile(currentUser, new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(PersonalInfoActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PersonalInfoActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(PersonalInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
