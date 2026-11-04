package com.example.healthmonitor.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "HealthMonitorPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_DEVICE_ID = "deviceId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_SAVED_PASSWORD = "savedPassword";
    private static final String KEY_IS_DARK_MODE = "isDarkMode";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLoginStatus(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public void saveDeviceId(String deviceId) {
        editor.putString(KEY_DEVICE_ID, deviceId);
        editor.apply();
    }

    public String getDeviceId() {
        return sharedPreferences.getString(KEY_DEVICE_ID, "DEFAULT_DEVICE");
    }

    public void saveLoginCredentials(String email, String password, boolean rememberMe) {
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        if (rememberMe) {
            editor.putString(KEY_USER_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            editor.remove(KEY_USER_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }
        editor.apply();
    }

    public boolean isRememberMeChecked() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getSavedEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getSavedPassword() {
        return sharedPreferences.getString(KEY_SAVED_PASSWORD, "");
    }

    public void setDarkMode(boolean isDarkMode) {
        editor.putBoolean(KEY_IS_DARK_MODE, isDarkMode);
        editor.apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_IS_DARK_MODE, false);
    }

    public void logout() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}
