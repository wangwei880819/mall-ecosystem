package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommissionConfig {
    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String rateType;
    private BigDecimal commissionRate;
    private String ladderConfig;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String status;
    private String settleType;
    private String settlePeriod;
    private BigDecimal minSettleAmount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getRateType() { return rateType; }
    public void setRateType(String rateType) { this.rateType = rateType; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public String getLadderConfig() { return ladderConfig; }
    public void setLadderConfig(String ladderConfig) { this.ladderConfig = ladderConfig; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public LocalDate getExpireDate() { return expireDate; }
    public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSettleType() { return settleType; }
    public void setSettleType(String settleType) { this.settleType = settleType; }
    public String getSettlePeriod() { return settlePeriod; }
    public void setSettlePeriod(String settlePeriod) { this.settlePeriod = settlePeriod; }
    public BigDecimal getMinSettleAmount() { return minSettleAmount; }
    public void setMinSettleAmount(BigDecimal minSettleAmount) { this.minSettleAmount = minSettleAmount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}