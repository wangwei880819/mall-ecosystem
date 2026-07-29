package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class DisposalConfig {
    private Long id;
    private String name;
    private String type;
    private String triggerRule;
    private String riskLevel;
    private String duration;
    private String status;
    private Integer execCount;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTriggerRule() { return triggerRule; }
    public void setTriggerRule(String triggerRule) { this.triggerRule = triggerRule; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getExecCount() { return execCount; }
    public void setExecCount(Integer execCount) { this.execCount = execCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
