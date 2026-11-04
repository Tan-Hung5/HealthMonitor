package com.example.healthmonitor.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.HealthMetric;
import com.example.healthmonitor.data.model.User;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.utils.PreferenceManager;

public class HomeFragment extends Fragment {

    private TextView tvHeartRate, tvSpo2, tvRespiratory, tvBloodPressure, tvGreeting;
    private TextView tvStatusTitle, tvStatusAdvice;
    private View viewStatusIndicator;
    private FirebaseHelper firebaseHelper;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvHeartRate = view.findViewById(R.id.tv_heart_rate);
        tvSpo2 = view.findViewById(R.id.tv_spo2);
        tvRespiratory = view.findViewById(R.id.tv_respiratory);
        tvBloodPressure = view.findViewById(R.id.tv_blood_pressure);
        
        // Ánh xạ các view đánh giá mới
        tvStatusTitle = view.findViewById(R.id.tv_status_title);
        tvStatusAdvice = view.findViewById(R.id.tv_status_advice);
        viewStatusIndicator = view.findViewById(R.id.view_status_indicator);

        firebaseHelper = FirebaseHelper.getInstance();
        prefManager = new PreferenceManager(requireContext());

        loadUserProfile();
        startObservingData();

        return view;
    }

    private void loadUserProfile() {
        String userId = prefManager.getUserId();
        if (!userId.isEmpty()) {
            firebaseHelper.fetchUserData(userId, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    if (isAdded() && user != null) {
                        tvGreeting.setText("Hello " + user.getName() + "!");
                    }
                }

                @Override
                public void onFailure(String message) {
                    // Fallback to default greeting
                }
            });
        }
    }

    private void startObservingData() {
        String deviceId = prefManager.getDeviceId();
        String userId = prefManager.getUserId();

        if (deviceId.isEmpty() || deviceId.equals("DEFAULT_DEVICE")) {
            updateAssessmentUI("Not Connected", "Please connect your device in the 'More' section to start tracking.", "#636E72");
            return;
        }

        firebaseHelper.observeRealtimeData(deviceId, userId, new FirebaseHelper.DataCallback<HealthMetric>() {
            @Override
            public void onDataReceived(HealthMetric data) {
                if (isAdded()) {
                    tvHeartRate.setText(String.valueOf(data.getHeartRate()));
                    tvSpo2.setText(String.format("%.1f", data.getSpo2()));
                    tvRespiratory.setText(String.valueOf(data.getRespiratory()));
                    tvBloodPressure.setText(data.getBloodPressure());
                    
                    // Thực hiện đánh giá dựa trên dữ liệu nhận được
                    performHealthAssessment(data);
                }
            }

            @Override
            public void onError(String message) {
                // Handle error
            }
        });
    }

    private void performHealthAssessment(HealthMetric data) {
        int hr = data.getHeartRate();
        float spo2 = data.getSpo2();
        
        if (spo2 < 90) {
            updateAssessmentUI("DANGEROUS WARNING", "Oxygen saturation (SpO2) is at a critically low level. Seek medical assistance immediately.", "#D63031");
        } else if (spo2 < 95 || hr > 110 || hr < 50) {
            updateAssessmentUI("Attention Needed", "Your vital signs show abnormal readings. Please sit down, rest and re-measure after a few minutes.", "#F1C40F");
        } else {
            updateAssessmentUI("Good Health", "Your vital signs are within normal ranges. Keep up the healthy lifestyle!", "#27AE60");
        }
    }

    private void updateAssessmentUI(String title, String advice, String colorHex) {
        tvStatusTitle.setText(title);
        tvStatusTitle.setTextColor(Color.parseColor(colorHex));
        tvStatusAdvice.setText(advice);
        viewStatusIndicator.setBackgroundColor(Color.parseColor(colorHex));
    }
}
