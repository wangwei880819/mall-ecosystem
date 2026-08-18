package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Deposit {
    private Long id;
    private String depositCode;
    private Long merchantId;
    private String depositType;
    private BigDecimal amount;
    private BigDecimal balance;
    private String payMethod;
    private String payNo;
    private String status;
    private String reason;
    private String approver;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDepositCode() { return depositCode; }
    public void setDepositCode(String depositCode) { this.depositCode = depositCode; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getDepositType() { return depositType; }
    public void setDepositType(String depositType) { this.depositType = depositType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public String getPayNo() { return payNo; }
    public void setPayNo(String payNo) { this.payNo = payNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public LocalDateTime getApproveTime() { return approveTime; }
    public void setApproveTime(LocalDateTime approveTime) { this.approveTime = approveTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}