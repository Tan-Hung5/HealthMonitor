package com.example.healthmonitor.data.model;

import java.io.Serializable;
import java.util.List;

public class DateHistoryGroup implements Serializable {
    private String date;
    private List<HealthMetric> metricsList;
    private boolean isExpanded;

    public DateHistoryGroup(String date, List<HealthMetric> metricsList) {
        this.date = date;
        this.metricsList = metricsList;
        this.isExpanded = false;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<HealthMetric> getMetricsList() { return metricsList; }
    public void setMetricsList(List<HealthMetric> metricsList) { this.metricsList = metricsList; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
