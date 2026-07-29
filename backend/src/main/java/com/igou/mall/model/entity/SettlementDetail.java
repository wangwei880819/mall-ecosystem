package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.util.Date;

public class SettlementDetail {
    private Long id;
    private Long settleId;
    private String orderCode;
    private BigDecimal orderAmount;
    private BigDecimal commissionRate;
    private BigDecimal commissionAmount;
    private BigDecimal aiDouAmount;
    private BigDecimal merchantAmount;
    private Date createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSettleId() { return settleId; }
    public void setSettleId(Long settleId) { this.settleId = settleId; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }
    public BigDecimal getAiDouAmount() { return aiDouAmount; }
    public void setAiDouAmount(BigDecimal aiDouAmount) { this.aiDouAmount = aiDouAmount; }
    public BigDecimal getMerchantAmount() { return merchantAmount; }
    public void setMerchantAmount(BigDecimal merchantAmount) { this.merchantAmount = merchantAmount; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}