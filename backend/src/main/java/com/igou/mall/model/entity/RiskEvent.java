package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class RiskEvent {
    private Long id;
    private String eventType;
    private String target;
    private String riskLevel;
    private Integer score;
    private String hitRule;
    private String status;
    private String source;
    private String detail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getHitRule() { return hitRule; }
    public void setHitRule(String hitRule) { this.hitRule = hitRule; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
