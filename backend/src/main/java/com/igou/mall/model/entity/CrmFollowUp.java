package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class CrmFollowUp {
    private Long id;
    private Long leadId;
    private String followType;
    private String content;
    private String nextPlan;
    private LocalDateTime nextFollowTime;
    private String followBy;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public String getFollowType() { return followType; }
    public void setFollowType(String followType) { this.followType = followType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNextPlan() { return nextPlan; }
    public void setNextPlan(String nextPlan) { this.nextPlan = nextPlan; }
    public LocalDateTime getNextFollowTime() { return nextFollowTime; }
    public void setNextFollowTime(LocalDateTime nextFollowTime) { this.nextFollowTime = nextFollowTime; }
    public String getFollowBy() { return followBy; }
    public void setFollowBy(String followBy) { this.followBy = followBy; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}