package com.igou.mall.model.entity;

import java.time.LocalDateTime;

public class BlacklistItem {
    private Long id;
    private String value;
    private String type;
    private String listType;
    private String reason;
    private String source;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getListType() { return listType; }
    public void setListType(String listType) { this.listType = listType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
}
