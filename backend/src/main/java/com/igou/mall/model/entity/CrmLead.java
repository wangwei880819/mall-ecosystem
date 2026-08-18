package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CrmLead {
    private Long id;
    private String leadCode;
    private String companyName;
    private String brandName;
    private String industry;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String source;
    private String status;
    private String intentionLevel;
    private BigDecimal estimatedGmv;
    private String assignedTo;
    private String lostReason;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLeadCode() { return leadCode; }
    public void setLeadCode(String leadCode) { this.leadCode = leadCode; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIntentionLevel() { return intentionLevel; }
    public void setIntentionLevel(String intentionLevel) { this.intentionLevel = intentionLevel; }
    public BigDecimal getEstimatedGmv() { return estimatedGmv; }
    public void setEstimatedGmv(BigDecimal estimatedGmv) { this.estimatedGmv = estimatedGmv; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getLostReason() { return lostReason; }
    public void setLostReason(String lostReason) { this.lostReason = lostReason; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}