package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Merchant {
    private Long id;
    private String merchantCode;
    private String merchantName;
    private String merchantType;
    private String creditCode;
    private String legalPerson;
    private String registeredCapital;
    private String businessScope;
    private String contactName;
    private String contactPhone;
    private String password;
    private String province;
    private String city;
    private String district;
    private String address;
    private String bankName;
    private String bankAccount;
    private String taxNumber;
    private Integer onboardingStep;
    private String onboardingStatus;
    private String riskLevel;
    private String merchantGrade;
    private BigDecimal commissionRate;
    private String settleAccount;
    private String settleCycle;
    private String status;
    private String rejectReason;
    private String industry;
    private Integer creditScore;
    private String legalPersonId;
    private String trademarkNo;
    private String authChain;
    private String categoryMatch;
    private String auditNode;
    private LocalDateTime auditNodeDeadline;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMerchantCode() { return merchantCode; }
    public void setMerchantCode(String merchantCode) { this.merchantCode = merchantCode; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getMerchantType() { return merchantType; }
    public void setMerchantType(String merchantType) { this.merchantType = merchantType; }
    public String getCreditCode() { return creditCode; }
    public void setCreditCode(String creditCode) { this.creditCode = creditCode; }
    public String getLegalPerson() { return legalPerson; }
    public void setLegalPerson(String legalPerson) { this.legalPerson = legalPerson; }
    public String getRegisteredCapital() { return registeredCapital; }
    public void setRegisteredCapital(String registeredCapital) { this.registeredCapital = registeredCapital; }
    public String getBusinessScope() { return businessScope; }
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    public Integer getOnboardingStep() { return onboardingStep; }
    public void setOnboardingStep(Integer onboardingStep) { this.onboardingStep = onboardingStep; }
    public String getOnboardingStatus() { return onboardingStatus; }
    public void setOnboardingStatus(String onboardingStatus) { this.onboardingStatus = onboardingStatus; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getMerchantGrade() { return merchantGrade; }
    public void setMerchantGrade(String merchantGrade) { this.merchantGrade = merchantGrade; }
    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) { this.commissionRate = commissionRate; }
    public String getSettleAccount() { return settleAccount; }
    public void setSettleAccount(String settleAccount) { this.settleAccount = settleAccount; }
    public String getSettleCycle() { return settleCycle; }
    public void setSettleCycle(String settleCycle) { this.settleCycle = settleCycle; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public String getLegalPersonId() { return legalPersonId; }
    public void setLegalPersonId(String legalPersonId) { this.legalPersonId = legalPersonId; }
    public String getTrademarkNo() { return trademarkNo; }
    public void setTrademarkNo(String trademarkNo) { this.trademarkNo = trademarkNo; }
    public String getAuthChain() { return authChain; }
    public void setAuthChain(String authChain) { this.authChain = authChain; }
    public String getCategoryMatch() { return categoryMatch; }
    public void setCategoryMatch(String categoryMatch) { this.categoryMatch = categoryMatch; }
    public String getAuditNode() { return auditNode; }
    public void setAuditNode(String auditNode) { this.auditNode = auditNode; }
    public LocalDateTime getAuditNodeDeadline() { return auditNodeDeadline; }
    public void setAuditNodeDeadline(LocalDateTime auditNodeDeadline) { this.auditNodeDeadline = auditNodeDeadline; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}