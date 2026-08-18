package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Contract {
    private Long id;
    private String contractCode;
    private Long merchantId;
    private Long templateId;
    private String contractType;
    private String contractTitle;
    private String contractContent;
    private String fileUrl;
    private String signUrl;
    private BigDecimal commissionRate;
    private BigDecimal depositAmount;
    private Integer platformSigned;
    private LocalDateTime platformSignTime;
    private String platformSigner;
    private Integer merchantSigned;
    private LocalDateTime merchantSignTime;
    private String status;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContractCode() { return contractCode; }
    public void setContractCode(String contractCode) { this.contractCode = contractCode; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    public String getContractTitle() { return contractTitle; }
    public void setContractTitle(String contractTitle) { this.contractTitle = contractTitle; }
    public String getContractContent() { return contractContent; }
    public void setContractContent(String contractContent) { this.contractContent = contractContent; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getSignUrl() { return signUrl; }
    public void setSignUrl(String signUrl) { this.signUrl = signUrl; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public Integer getPlatformSigned() { return platformSigned; }
    public void setPlatformSigned(Integer platformSigned) { this.platformSigned = platformSigned; }
    public LocalDateTime getPlatformSignTime() { return platformSignTime; }
    public void setPlatformSignTime(LocalDateTime platformSignTime) { this.platformSignTime = platformSignTime; }
    public String getPlatformSigner() { return platformSigner; }
    public void setPlatformSigner(String platformSigner) { this.platformSigner = platformSigner; }
    public Integer getMerchantSigned() { return merchantSigned; }
    public void setMerchantSigned(Integer merchantSigned) { this.merchantSigned = merchantSigned; }
    public LocalDateTime getMerchantSignTime() { return merchantSignTime; }
    public void setMerchantSignTime(LocalDateTime merchantSignTime) { this.merchantSignTime = merchantSignTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public LocalDate getExpireDate() { return expireDate; }
    public void setExpireDate(LocalDate expireDate) { this.expireDate = expireDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}