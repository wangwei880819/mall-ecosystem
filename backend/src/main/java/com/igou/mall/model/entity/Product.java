package com.igou.mall.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private Long id;
    private String productCode;
    private String productName;
    private String productType;        // 商品类型: PHYSICAL/VIRTUAL/BENEFIT
    private Long categoryId;
    private String category;
    private String brand;
    private Long merchantId;
    private String merchantName;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private BigDecimal vipPrice;
    private Integer stock;
    private Integer salesCount;
    private BigDecimal avgScore;
    private String description;
    private String detail;
    private String imageUrls;
    private String aiSellingPoint;
    private String aiTag;
    private String status;
    private BigDecimal weight;
    private BigDecimal volume;
    private String origin;
    private String spec;
    private Integer isHot;
    private Integer isNew;
    private Integer isRecommend;
    private Integer sortOrder;
    private LocalDateTime auditTime;
    private String auditor;
    private String rejectReason;
    private String approveReason;
    private Integer reviewLevel;    // 审核级别：1-一级选品审核 2-二级选品审核 null-未审核
    private LocalDateTime level1AuditTime;  // 一级审核时间
    private String level1Auditor;           // 一级审核人
    private LocalDateTime level2AuditTime;  // 二级审核时间
    private String level2Auditor;           // 二级审核人
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getMarketPrice() { return marketPrice; }
    public void setMarketPrice(BigDecimal marketPrice) { this.marketPrice = marketPrice; }
    public BigDecimal getVipPrice() { return vipPrice; }
    public void setVipPrice(BigDecimal vipPrice) { this.vipPrice = vipPrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getSalesCount() { return salesCount; }
    public void setSalesCount(Integer salesCount) { this.salesCount = salesCount; }
    public BigDecimal getAvgScore() { return avgScore; }
    public void setAvgScore(BigDecimal avgScore) { this.avgScore = avgScore; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public String getAiSellingPoint() { return aiSellingPoint; }
    public void setAiSellingPoint(String aiSellingPoint) { this.aiSellingPoint = aiSellingPoint; }
    public String getAiTag() { return aiTag; }
    public void setAiTag(String aiTag) { this.aiTag = aiTag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public Integer getIsHot() { return isHot; }
    public void setIsHot(Integer isHot) { this.isHot = isHot; }
    public Integer getIsNew() { return isNew; }
    public void setIsNew(Integer isNew) { this.isNew = isNew; }
    public Integer getIsRecommend() { return isRecommend; }
    public void setIsRecommend(Integer isRecommend) { this.isRecommend = isRecommend; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
    public String getAuditor() { return auditor; }
    public void setAuditor(String auditor) { this.auditor = auditor; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getApproveReason() { return approveReason; }
    public void setApproveReason(String approveReason) { this.approveReason = approveReason; }
    public Integer getReviewLevel() { return reviewLevel; }
    public void setReviewLevel(Integer reviewLevel) { this.reviewLevel = reviewLevel; }
    public LocalDateTime getLevel1AuditTime() { return level1AuditTime; }
    public void setLevel1AuditTime(LocalDateTime level1AuditTime) { this.level1AuditTime = level1AuditTime; }
    public String getLevel1Auditor() { return level1Auditor; }
    public void setLevel1Auditor(String level1Auditor) { this.level1Auditor = level1Auditor; }
    public LocalDateTime getLevel2AuditTime() { return level2AuditTime; }
    public void setLevel2AuditTime(LocalDateTime level2AuditTime) { this.level2AuditTime = level2AuditTime; }
    public String getLevel2Auditor() { return level2Auditor; }
    public void setLevel2Auditor(String level2Auditor) { this.level2Auditor = level2Auditor; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}