package com.example.healthmonitor.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthmonitor.R;
import com.example.healthmonitor.data.model.DateHistoryGroup;
import com.example.healthmonitor.data.model.HealthMetric;
import java.util.List;
import java.util.Locale;

public class DateHistoryAdapter extends RecyclerView.Adapter<DateHistoryAdapter.GroupViewHolder> {

    private List<DateHistoryGroup> groupList;
    private OnMetricClickListener listener;

    public interface OnMetricClickListener {
        void onMetricClick(HealthMetric metric);
    }

    public DateHistoryAdapter(List<DateHistoryGroup> groupList, OnMetricClickListener listener) {
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        DateHistoryGroup group = groupList.get(position);

        holder.tvDate.setText("Ngày: " + group.getDate());
        int count = group.getMetricsList() != null ? group.getMetricsList().size() : 0;
        holder.tvCount.setText(count + " lần đo");

        if (group.isExpanded()) {
            holder.rvTimeList.setVisibility(View.VISIBLE);
            holder.imgArrow.setRotation(270); // Arrow pointing up
        } else {
            holder.rvTimeList.setVisibility(View.GONE);
            holder.imgArrow.setRotation(90);  // Arrow pointing down
        }

        // Setup sub RecyclerView for times
        holder.rvTimeList.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        TimeHistoryAdapter subAdapter = new TimeHistoryAdapter(group.getMetricsList(), listener);
        holder.rvTimeList.setAdapter(subAdapter);

        holder.layoutHeader.setOnClickListener(v -> {
            boolean expanded = !group.isExpanded();
            group.setExpanded(expanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return groupList != null ? groupList.size() : 0;
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutHeader;
        TextView tvDate, tvCount;
        ImageView imgArrow;
        RecyclerView rvTimeList;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutHeader = itemView.findViewById(R.id.layout_date_header);
            tvDate = itemView.findViewById(R.id.tv_group_date);
            tvCount = itemView.findViewById(R.id.tv_group_count);
            imgArrow = itemView.findViewById(R.id.img_expand_arrow);
            rvTimeList = itemView.findViewById(R.id.rv_time_list);
        }
    }

    // Inner Sub-Adapter for Measurement Times under each Date
    private static class TimeHistoryAdapter extends RecyclerView.Adapter<TimeHistoryAdapter.TimeViewHolder> {

        private List<HealthMetric> timeList;
        private OnMetricClickListener listener;

        public TimeHistoryAdapter(List<HealthMetric> timeList, OnMetricClickListener listener) {
            this.timeList = timeList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_sub_item, parent, false);
            return new TimeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
            HealthMetric metric = timeList.get(position);

            String timeDisplay = metric.getTimeKey() != null ? metric.getTimeKey() : "Lần đo " + (position + 1);
            holder.tvTime.setText(timeDisplay);

            String summary = String.format(Locale.getDefault(),
                    "HR: %d BPM | SpO2: %.1f%% | BP: %s",
                    metric.getHeartRate(),
                    metric.getSpo2(),
                    metric.getBloodPressure());
            holder.tvSummary.setText(summary);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMetricClick(metric);
                }
            });
        }

        @Override
        public int getItemCount() {
            return timeList != null ? timeList.size() : 0;
        }

        public static class TimeViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime, tvSummary;

            public TimeViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTime = itemView.findViewById(R.id.tv_sub_time);
                tvSummary = itemView.findViewById(R.id.tv_sub_summary);
            }
        }
    }
}
