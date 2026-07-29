package com.igou.mall.model.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionMonitor {
    private Long id;
    private String monitorCode;
    private String monitorType;
    private String metricType;
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private Integer isBreach;
    private String status;
    private LocalDateTime monitorTime;
    private String remark;
    private LocalDateTime createTime;
}