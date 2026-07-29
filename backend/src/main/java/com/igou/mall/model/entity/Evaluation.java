package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class Evaluation {
    private Long id;
    private Long orderId;
    private Long merchantId;
    private Long productId;
    private String userPhone;
    private Integer scoreQuality;
    private Integer scoreDelivery;
    private Integer scoreService;
    private Integer scoreAftersale;
    private Integer scoreValue;
    private String content;
    private String tags;
    private String sentiment;
    private String aiStatus;
    private String merchantReply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public Integer getScoreQuality() { return scoreQuality; }
    public void setScoreQuality(Integer scoreQuality) { this.scoreQuality = scoreQuality; }
    public Integer getScoreDelivery() { return scoreDelivery; }
    public void setScoreDelivery(Integer scoreDelivery) { this.scoreDelivery = scoreDelivery; }
    public Integer getScoreService() { return scoreService; }
    public void setScoreService(Integer scoreService) { this.scoreService = scoreService; }
    public Integer getScoreAftersale() { return scoreAftersale; }
    public void setScoreAftersale(Integer scoreAftersale) { this.scoreAftersale = scoreAftersale; }
    public Integer getScoreValue() { return scoreValue; }
    public void setScoreValue(Integer scoreValue) { this.scoreValue = scoreValue; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    public String getAiStatus() { return aiStatus; }
    public void setAiStatus(String aiStatus) { this.aiStatus = aiStatus; }
    public String getMerchantReply() { return merchantReply; }
    public void setMerchantReply(String merchantReply) { this.merchantReply = merchantReply; }
    public LocalDateTime getReplyTime() { return replyTime; }
    public void setReplyTime(LocalDateTime replyTime) { this.replyTime = replyTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
