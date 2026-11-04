package com.example.healthmonitor.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.DateHistoryGroup;
import com.example.healthmonitor.data.model.HealthMetric;
import com.example.healthmonitor.data.remote.FirebaseHelper;
import com.example.healthmonitor.utils.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private FirebaseHelper firebaseHelper;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        rvHistory = view.findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseHelper = FirebaseHelper.getInstance();
        prefManager = new PreferenceManager(requireContext());

        loadHistory();

        return view;
    }

    private void loadHistory() {
        String userId = prefManager.getUserId();

        if (userId.isEmpty()) return;

        firebaseHelper.fetchGroupedHistoryData(userId, new FirebaseHelper.DataCallback<List<DateHistoryGroup>>() {
            @Override
            public void onDataReceived(List<DateHistoryGroup> groupList) {
                if (isAdded()) {
                    DateHistoryAdapter adapter = new DateHistoryAdapter(groupList, metric -> showDetailDialog(metric));
                    rvHistory.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDetailDialog(HealthMetric metric) {
        String dateStr = metric.getDate() != null ? metric.getDate() : "N/A";
        String timeStr = metric.getTimeKey() != null ? metric.getTimeKey() : "N/A";

        if (metric.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            timeStr += " (Recorded at " + sdf.format(new Date(metric.getTimestamp())) + ")";
        }

        String message = "Date: " + dateStr + "\n" +
                "Time: " + timeStr + "\n" +
                "Device: " + (metric.getDeviceId() != null && !metric.getDeviceId().isEmpty() ? metric.getDeviceId() : "N/A") + "\n" +
                "Heart Rate: " + metric.getHeartRate() + " BPM\n" +
                "SpO2: " + metric.getSpo2() + " %\n" +
                "Respiratory Rate: " + metric.getRespiratory() + " breaths/min\n" +
                "Blood Pressure: " + metric.getBloodPressure() + " mmHg";

        new AlertDialog.Builder(requireContext())
                .setTitle("Measurement Details")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
}
