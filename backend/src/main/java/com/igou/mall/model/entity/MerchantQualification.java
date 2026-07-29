package com.igou.mall.model.entity;

import java.util.Date;

public class MerchantQualification {
    private Long id;
    private Long merchantId;
    private String qualType;
    private String qualName;
    private String qualFileUrl;
    private String auditStatus;
    private Date auditTime;
    private String auditor;
    private String auditComment;
    private Date expireDate;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getQualType() { return qualType; }
    public void setQualType(String qualType) { this.qualType = qualType; }
    public String getQualName() { return qualName; }
    public void setQualName(String qualName) { this.qualName = qualName; }
    public String getQualFileUrl() { return qualFileUrl; }
    public void setQualFileUrl(String qualFileUrl) { this.qualFileUrl = qualFileUrl; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditor() { return auditor; }
    public void setAuditor(String auditor) { this.auditor = auditor; }
    public String getAuditComment() { return auditComment; }
    public void setAuditComment(String auditComment) { this.auditComment = auditComment; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}