package com.example.healthmonitor.handler;

import com.example.healthmonitor.data.model.HealthMetric;
import java.util.List;

/**
 * Handler chịu trách nhiệm điều phối dữ liệu giữa UI và Data Layer.
 */
public class HealthDataHandler {
    
    public HealthDataHandler() {
        // Khởi tạo
    }

    public void processNewMetric(HealthMetric metric) {
        // Logic xử lý: Lưu SQLite trước, sau đó đồng bộ lên Firebase
    }

    public String analyzeHeartRate(int bpm) {
        if (bpm < 60) return "Thấp";
        if (bpm > 100) return "Cao";
        return "Bình thường";
    }
}