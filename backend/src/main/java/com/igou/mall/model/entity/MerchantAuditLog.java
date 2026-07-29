package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class MerchantAuditLog {
    private Long id;
    private Long merchantId;
    private String auditNode;      // 审核节点：QUALIFICATION/BUSINESS/COMPLIANCE/CONTRACT/PAYMENT
    private String action;         // APPROVED / REJECTED
    private String operator;       // 操作人
    private String comment;        // 审核说明
    private String rejectReason;   // 驳回原因
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getAuditNode() { return auditNode; }
    public void setAuditNode(String auditNode) { this.auditNode = auditNode; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
