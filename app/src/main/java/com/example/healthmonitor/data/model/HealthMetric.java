package com.example.healthmonitor.data.model;

import java.io.Serializable;
import com.google.firebase.database.PropertyName;

public class HealthMetric implements Serializable {
    private String deviceId;
    private int heartRate;
    private int respiratory;
    private float spo2;
    private String bloodPressure;
    private long timestamp;
    private int systolicBp;
    private int diatolicBp;
    private String date;
    private String timeKey;

    public HealthMetric() {}

    public HealthMetric(String deviceId, int heartRate, int respiratory, float spo2, String bloodPressure, long timestamp) {
        this.deviceId = deviceId;
        this.heartRate = heartRate;
        this.respiratory = respiratory;
        this.spo2 = spo2;
        this.bloodPressure = bloodPressure;
        this.timestamp = timestamp;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTimeKey() { return timeKey; }
    public void setTimeKey(String timeKey) { this.timeKey = timeKey; }

    @PropertyName("heart_rate")
    public int getHeartRate() { return heartRate; }
    
    @PropertyName("heart_rate")
    public void setHeartRate(Object val) {
        this.heartRate = parseInteger(val);
    }

    @PropertyName("respiratory_rate")
    public int getRespiratory() { return respiratory; }
    
    @PropertyName("respiratory_rate")
    public void setRespiratory(Object val) {
        this.respiratory = parseInteger(val);
    }

    public float getSpo2() { return spo2; }
    
    public void setSpo2(Object val) {
        this.spo2 = parseFloat(val);
    }

    public String getBloodPressure() { 
        if (bloodPressure != null && !bloodPressure.isEmpty()) {
            return bloodPressure;
        }
        return systolicBp + "/" + diatolicBp;
    }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }

    @PropertyName("systolic_bp")
    public int getSystolicBp() { return systolicBp; }
    
    @PropertyName("systolic_bp")
    public void setSystolicBp(Object val) {
        this.systolicBp = parseInteger(val);
    }

    @PropertyName("diastolic_bp")
    public int getDiastolicBp() { return diatolicBp; }
    
    @PropertyName("diastolic_bp")
    public void setDiastolicBp(Object val) {
        this.diatolicBp = parseInteger(val);
    }

    @PropertyName("diatolic_bp")
    public int getDiatolicBp() { return diatolicBp; }
    
    @PropertyName("diatolic_bp")
    public void setDiatolicBp(Object val) {
        this.diatolicBp = parseInteger(val);
    }

    @PropertyName("update_at")
    public long getTimestamp() { return timestamp; }

    @PropertyName("update_at")
    public void setUpdateAt(Object val) {
        this.timestamp = parseLong(val);
    }

    public void setTimestamp(Object val) {
        this.timestamp = parseLong(val);
    }

    private int parseInteger(Object val) {
        if (val instanceof Number) {
            return ((Number) val).intValue();
        } else if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private float parseFloat(Object val) {
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        } else if (val instanceof String) {
            try {
                return Float.parseFloat((String) val);
            } catch (Exception ignored) {}
        }
        return 0f;
    }

    private long parseLong(Object val) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (Exception ignored) {}
        }
        return 0L;
    }
}
