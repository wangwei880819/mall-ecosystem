package com.igou.mall.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SyncMonitorService {

    @Autowired
    private DataSyncService dataSyncService;

    @Autowired
    private DataConsistencyService consistencyService;

    private final ConcurrentLinkedQueue<SyncEvent> syncEvents = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<AlertEvent> alertEvents = new ConcurrentLinkedQueue<>();

    private final AtomicLong totalSyncCount = new AtomicLong(0);
    private final AtomicLong successSyncCount = new AtomicLong(0);
    private final AtomicLong failedSyncCount = new AtomicLong(0);

    private volatile long lastSyncTime = 0;
    private volatile long lastAlertTime = 0;

    public void recordSyncEvent(String syncType, Long targetId, boolean success, String message) {
        SyncEvent event = new SyncEvent();
        event.setSyncType(syncType);
        event.setTargetId(targetId);
        event.setSuccess(success);
        event.setMessage(message);
        event.setTimestamp(System.currentTimeMillis());

        syncEvents.offer(event);

        if (syncEvents.size() > 1000) {
            syncEvents.poll();
        }

        totalSyncCount.incrementAndGet();
        if (success) {
            successSyncCount.incrementAndGet();
        } else {
            failedSyncCount.incrementAndGet();
            if (System.currentTimeMillis() - lastAlertTime > 60000) {
                createAlert("SYNC_FAILURE", syncType + "同步失败: " + message);
            }
        }

        lastSyncTime = System.currentTimeMillis();
    }

    public void createAlert(String alertType, String message) {
        AlertEvent alert = new AlertEvent();
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setTimestamp(System.currentTimeMillis());
        alert.setStatus("ACTIVE");

        alertEvents.offer(alert);

        if (alertEvents.size() > 100) {
            alertEvents.poll();
        }

        lastAlertTime = System.currentTimeMillis();
    }

    public Map<String, Object> getMonitorStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSyncCount", totalSyncCount.get());
        stats.put("successSyncCount", successSyncCount.get());
        stats.put("failedSyncCount", failedSyncCount.get());
        stats.put("successRate", calculateSuccessRate());
        stats.put("lastSyncTime", lastSyncTime > 0 ? LocalDateTime.now().toString() : "N/A");
        stats.put("lastAlertTime", lastAlertTime > 0 ? LocalDateTime.now().toString() : "N/A");
        stats.put("activeAlertCount", getActiveAlertCount());
        stats.put("monitorTime", LocalDateTime.now().toString());

        return stats;
    }

    private double calculateSuccessRate() {
        long total = totalSyncCount.get();
        if (total == 0) return 100.0;
        return Math.round((successSyncCount.get() * 100.0 / total) * 100) / 100.0;
    }

    public int getActiveAlertCount() {
        int count = 0;
        for (AlertEvent alert : alertEvents) {
            if ("ACTIVE".equals(alert.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public List<Map<String, Object>> getRecentSyncEvents(int limit) {
        List<Map<String, Object>> events = new ArrayList<>();
        Object[] eventArray = syncEvents.toArray();

        int start = Math.max(0, eventArray.length - limit);
        for (int i = start; i < eventArray.length; i++) {
            SyncEvent event = (SyncEvent) eventArray[i];
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("syncType", event.getSyncType());
            eventMap.put("targetId", event.getTargetId());
            eventMap.put("success", event.isSuccess());
            eventMap.put("message", event.getMessage());
            eventMap.put("timestamp", LocalDateTime.now().toString());
            events.add(eventMap);
        }

        return events;
    }

    public List<Map<String, Object>> getAlertEvents() {
        List<Map<String, Object>> events = new ArrayList<>();
        for (AlertEvent alert : alertEvents) {
            Map<String, Object> alertMap = new HashMap<>();
            alertMap.put("alertType", alert.getAlertType());
            alertMap.put("message", alert.getMessage());
            alertMap.put("timestamp", LocalDateTime.now().toString());
            alertMap.put("status", alert.getStatus());
            events.add(alertMap);
        }
        return events;
    }

    public void acknowledgeAlert(int index) {
        Object[] eventArray = alertEvents.toArray();
        if (index >= 0 && index < eventArray.length) {
            AlertEvent alert = (AlertEvent) eventArray[index];
            alert.setStatus("ACKNOWLEDGED");
        }
    }

    public void clearAlerts() {
        alertEvents.clear();
    }

    @Scheduled(fixedRate = 60000)
    public void checkSyncHealth() {
        long now = System.currentTimeMillis();
        if (now - lastSyncTime > 300000) {
            createAlert("SYNC_TIMEOUT", "数据同步超时，超过5分钟未同步");
        }

        Map<String, Object> consistencyResult = consistencyService.validateAllData();
        int inconsistencyCount = (Integer) consistencyResult.get("totalInconsistencies");
        if (inconsistencyCount > 0) {
            createAlert("DATA_INCONSISTENCY", "检测到" + inconsistencyCount + "条数据不一致");
        }
    }

    @Scheduled(fixedRate = 1800000)
    public void generateDailyReport() {
        Map<String, Object> report = getMonitorStats();
        report.put("reportType", "DAILY");
        report.put("reportTime", LocalDateTime.now().toString());
    }

    private static class SyncEvent {
        private String syncType;
        private Long targetId;
        private boolean success;
        private String message;
        private long timestamp;

        public String getSyncType() { return syncType; }
        public void setSyncType(String syncType) { this.syncType = syncType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    private static class AlertEvent {
        private String alertType;
        private String message;
        private long timestamp;
        private String status;

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}