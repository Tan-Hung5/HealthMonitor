package com.example.healthmonitor.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.HealthMetric;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HealthMetric> historyList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(HealthMetric metric);
    }

    public HistoryAdapter(List<HealthMetric> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthMetric metric = historyList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        
        holder.tvTime.setText(sdf.format(new Date(metric.getTimestamp())));
        
        // Hiển thị thêm Device ID ở phần tóm tắt
        String summary = String.format(Locale.getDefault(), 
                "Dev: %s | HR: %d | SpO2: %.1f%%", 
                metric.getDeviceId() != null ? metric.getDeviceId() : "N/A",
                metric.getHeartRate(), 
                metric.getSpo2());
        
        holder.tvSummary.setText(summary);
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(metric));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvSummary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_history_time);
            tvSummary = itemView.findViewById(R.id.tv_history_summary);
        }
    }
}
