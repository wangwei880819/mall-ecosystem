package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class RiskRule {
    private Long id;
    private String name;
    private String type;
    private String scene;
    private Integer priority;
    private String ruleCondition;
    private String action;
    private Integer hitCount;
    private Boolean active;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getRuleCondition() { return ruleCondition; }
    public void setRuleCondition(String ruleCondition) { this.ruleCondition = ruleCondition; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getHitCount() { return hitCount; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
