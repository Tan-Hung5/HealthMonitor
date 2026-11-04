package com.example.healthmonitor.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.ui.auth.LoginActivity;
import com.example.healthmonitor.ui.auth.PersonalInfoActivity;
import com.example.healthmonitor.ui.base.BaseActivity;
import com.example.healthmonitor.utils.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private ImageButton btnThemeToggle;
    private PreferenceManager prefManager;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefManager = new PreferenceManager(this);
        applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseHelper = FirebaseHelper.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationView = findViewById(R.id.navigation_view);
        btnThemeToggle = findViewById(R.id.btn_theme_toggle);

        updateThemeIcon();
        
        btnThemeToggle.setOnClickListener(v -> toggleDarkMode());

        View btnChatbot = findViewById(R.id.btn_chatbot_bubble);
        if (btnChatbot != null) {
            btnChatbot.setOnClickListener(v -> showChatbotDialog());
        }

        setupBottomNavigation();
        setupSideNavigation();
        updateNavHeader();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void showChatbotDialog() {
        ChatbotBottomSheetDialog dialog = new ChatbotBottomSheetDialog();
        dialog.show(getSupportFragmentManager(), "ChatbotBottomSheetDialog");
    }

    private void applyTheme() {
        if (prefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void updateThemeIcon() {
        if (btnThemeToggle != null) {
            if (prefManager.isDarkMode()) {
                btnThemeToggle.setImageResource(R.drawable.ic_light_mode);
            } else {
                btnThemeToggle.setImageResource(R.drawable.ic_dark_mode);
            }
        }
    }

    private void toggleDarkMode() {
        boolean newDarkModeStatus = !prefManager.isDarkMode();
        prefManager.setDarkMode(newDarkModeStatus);
        applyTheme();
        recreate();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_history) {
                loadFragment(new HistoryFragment());
                return true;
            } else if (itemId == R.id.nav_more) {
                drawerLayout.openDrawer(GravityCompat.END);
                return true;
            }
            return false;
        });
    }

    private void setupSideNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_personal_info) {
                startActivity(new Intent(MainActivity.this, PersonalInfoActivity.class));
            } else if (itemId == R.id.menu_change_device) {
                showChangeDeviceDialog();
            } else if (itemId == R.id.menu_disconnect_device) {
                confirmAndDisconnectDevice();
            } else if (itemId == R.id.menu_logout) {
                performLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });
    }

    private void confirmAndDisconnectDevice() {
        String currentDeviceId = prefManager.getDeviceId();
        if (currentDeviceId.isEmpty() || currentDeviceId.equals("DEFAULT_DEVICE")) {
            Toast.makeText(this, "No device is currently connected.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Disconnect Device")
                .setMessage("Are you sure you want to disconnect from device " + currentDeviceId + "?")
                .setPositiveButton("Disconnect", (dialog, which) -> disconnectCurrentDevice(currentDeviceId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void disconnectCurrentDevice(String deviceId) {
        firebaseHelper.disconnectDevice(deviceId, new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                prefManager.saveDeviceId("DEFAULT_DEVICE");
                Toast.makeText(MainActivity.this, "Disconnected from device " + deviceId, Toast.LENGTH_SHORT).show();
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (currentFragment instanceof HomeFragment) {
                    loadFragment(new HomeFragment());
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(MainActivity.this, "Failed to disconnect: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    private void showChangeDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Device");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter device ID (e.g. DEV001)");
        builder.setView(input);

        builder.setPositiveButton("Connect", (dialog, which) -> {
            String deviceId = input.getText().toString().trim();
            if (!deviceId.isEmpty()) {
                checkAndConnectDevice(deviceId);
            } else {
                Toast.makeText(this, "Please enter a device ID", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void checkAndConnectDevice(String deviceId) {
        String currentDeviceId = prefManager.getDeviceId();
        String userId = prefManager.getUserId();
        firebaseHelper.checkAndConnectDevice(deviceId, currentDeviceId, userId, new FirebaseHelper.ActionCallback() {
            @Override
            public void onSuccess() {
                prefManager.saveDeviceId(deviceId);
                Toast.makeText(MainActivity.this, "Device " + deviceId + " connected successfully!", Toast.LENGTH_SHORT).show();
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (currentFragment instanceof HomeFragment) {
                    loadFragment(new HomeFragment());
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        TextView tvName = headerView.findViewById(R.id.tv_user_name);
        TextView tvEmail = headerView.findViewById(R.id.tv_user_email);

        String userId = prefManager.getUserId();
        if (!userId.isEmpty()) {
            firebaseHelper.fetchUserData(userId, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    if (tvName != null) tvName.setText(user.getName());
                    if (tvEmail != null) tvEmail.setText(user.getEmail());
                }

                @Override
                public void onFailure(String message) {
                    if (tvEmail != null) tvEmail.setText(prefManager.getSavedEmail());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
    }

    private void performLogout() {
        prefManager.setLoginStatus(false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
