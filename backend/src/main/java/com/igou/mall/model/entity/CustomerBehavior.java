package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class CustomerBehavior {
    private Long id;
    private Long customerId;
    private String behaviorType;
    private String behaviorDetail;
    private Long targetId;
    private LocalDateTime behaviorTime;
    private String ipAddress;
    private String deviceInfo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getBehaviorType() { return behaviorType; }
    public void setBehaviorType(String behaviorType) { this.behaviorType = behaviorType; }
    public String getBehaviorDetail() { return behaviorDetail; }
    public void setBehaviorDetail(String behaviorDetail) { this.behaviorDetail = behaviorDetail; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public LocalDateTime getBehaviorTime() { return behaviorTime; }
    public void setBehaviorTime(LocalDateTime behaviorTime) { this.behaviorTime = behaviorTime; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
}