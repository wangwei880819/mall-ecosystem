package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.util.Date;

public class ReconciliationRecord {
    private Long id;
    private String reconCode;
    private String reconPeriod;
    private Integer totalOrderCount;
    private BigDecimal totalOrderAmount;
    private Integer payOrderCount;
    private BigDecimal payOrderAmount;
    private Integer diffCount;
    private BigDecimal diffAmount;
    private String status;
    private String resultDetail;
    private Date createTime;
    private Date updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReconCode() { return reconCode; }
    public void setReconCode(String reconCode) { this.reconCode = reconCode; }
    public String getReconPeriod() { return reconPeriod; }
    public void setReconPeriod(String reconPeriod) { this.reconPeriod = reconPeriod; }
    public Integer getTotalOrderCount() { return totalOrderCount; }
    public void setTotalOrderCount(Integer totalOrderCount) { this.totalOrderCount = totalOrderCount; }
    public BigDecimal getTotalOrderAmount() { return totalOrderAmount; }
    public void setTotalOrderAmount(BigDecimal totalOrderAmount) { this.totalOrderAmount = totalOrderAmount; }
    public Integer getPayOrderCount() { return payOrderCount; }
    public void setPayOrderCount(Integer payOrderCount) { this.payOrderCount = payOrderCount; }
    public BigDecimal getPayOrderAmount() { return payOrderAmount; }
    public void setPayOrderAmount(BigDecimal payOrderAmount) { this.payOrderAmount = payOrderAmount; }
    public Integer getDiffCount() { return diffCount; }
    public void setDiffCount(Integer diffCount) { this.diffCount = diffCount; }
    public BigDecimal getDiffAmount() { return diffAmount; }
    public void setDiffAmount(BigDecimal diffAmount) { this.diffAmount = diffAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultDetail() { return resultDetail; }
    public void setResultDetail(String resultDetail) { this.resultDetail = resultDetail; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}