package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 权益商品实体
 * 用于会员权益、优惠券、数字内容、服务保障等非实物虚拟权益的统一管理
 */
public class Benefit {
    private Long id;
    private String benefitCode;        // 权益编号
    private String benefitName;        // 权益名称
    private Long merchantId;           // 商户ID
    private String benefitType;        // 权益类型: MEMBERSHIP/COUPON/GAME_POINTS/DIGITAL_CONTENT/SERVICE/INSURANCE
    private BigDecimal faceValue;      // 面值/原价
    private BigDecimal price;          // 售价
    private BigDecimal settlePrice;    // 结算价
    private String validityType;       // 有效期类型: FIXED_DATE/DAYS_AFTER_RECEIVE/DURATION
    private LocalDateTime validityStart; // 有效期开始
    private LocalDateTime validityEnd;   // 有效期结束
    private Integer validityDays;      // 有效天数（领取后N天有效）
    private String usageRules;         // 使用规则（JSON或文本描述）
    private String applicableScope;    // 适用范围
    private String exchangeMethod;     // 兑换方式: AUTO_BIND/CODE/QR_CODE/MANUAL
    private Integer stockTotal;        // 总库存
    private Integer stockUsed;         // 已使用/已兑换
    private Integer stockDailyLimit;   // 每日限兑数量
    private Integer stockPerUser;      // 每人限兑数量
    private String supplierName;       // 供应商名称
    private String supplierContact;    // 供应商联系方式
    private String refundPolicy;       // 退款政策: NO_REFUND/CONDITIONAL/FULL_REFUND
    private String imageUrl;           // 封面图片URL
    private String detailDesc;         // 详细说明（富文本）
    private String benefitDescription; // 权益描述
    private String aiTag;              // AI卖点标签
    private String aiSellingPoint;     // AI卖点描述
    private String status;             // 状态: PENDING/ON_SHELF/OFF_SHELF/REJECTED
    private String auditor;            // 审核人
    private LocalDateTime auditTime;   // 审核时间
    private String rejectReason;       // 驳回原因
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ========== Getters & Setters ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBenefitCode() { return benefitCode; }
    public void setBenefitCode(String benefitCode) { this.benefitCode = benefitCode; }
    public String getBenefitName() { return benefitName; }
    public void setBenefitName(String benefitName) { this.benefitName = benefitName; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getBenefitType() { return benefitType; }
    public void setBenefitType(String benefitType) { this.benefitType = benefitType; }
    public BigDecimal getFaceValue() { return faceValue; }
    public void setFaceValue(BigDecimal faceValue) { this.faceValue = faceValue; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getSettlePrice() { return settlePrice; }
    public void setSettlePrice(BigDecimal settlePrice) { this.settlePrice = settlePrice; }
    public String getValidityType() { return validityType; }
    public void setValidityType(String validityType) { this.validityType = validityType; }
    public LocalDateTime getValidityStart() { return validityStart; }
    public void setValidityStart(LocalDateTime validityStart) { this.validityStart = validityStart; }
    public LocalDateTime getValidityEnd() { return validityEnd; }
    public void setValidityEnd(LocalDateTime validityEnd) { this.validityEnd = validityEnd; }
    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }
    public String getUsageRules() { return usageRules; }
    public void setUsageRules(String usageRules) { this.usageRules = usageRules; }
    public String getApplicableScope() { return applicableScope; }
    public void setApplicableScope(String applicableScope) { this.applicableScope = applicableScope; }
    public String getExchangeMethod() { return exchangeMethod; }
    public void setExchangeMethod(String exchangeMethod) { this.exchangeMethod = exchangeMethod; }
    public Integer getStockTotal() { return stockTotal; }
    public void setStockTotal(Integer stockTotal) { this.stockTotal = stockTotal; }
    public Integer getStockUsed() { return stockUsed; }
    public void setStockUsed(Integer stockUsed) { this.stockUsed = stockUsed; }
    public Integer getStockDailyLimit() { return stockDailyLimit; }
    public void setStockDailyLimit(Integer stockDailyLimit) { this.stockDailyLimit = stockDailyLimit; }
    public Integer getStockPerUser() { return stockPerUser; }
    public void setStockPerUser(Integer stockPerUser) { this.stockPerUser = stockPerUser; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }
    public String getRefundPolicy() { return refundPolicy; }
    public void setRefundPolicy(String refundPolicy) { this.refundPolicy = refundPolicy; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDetailDesc() { return detailDesc; }
    public void setDetailDesc(String detailDesc) { this.detailDesc = detailDesc; }
    public String getBenefitDescription() { return benefitDescription; }
    public void setBenefitDescription(String benefitDescription) { this.benefitDescription = benefitDescription; }
    public String getAiTag() { return aiTag; }
    public void setAiTag(String aiTag) { this.aiTag = aiTag; }
    public String getAiSellingPoint() { return aiSellingPoint; }
    public void setAiSellingPoint(String aiSellingPoint) { this.aiSellingPoint = aiSellingPoint; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditor() { return auditor; }
    public void setAuditor(String auditor) { this.auditor = auditor; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
