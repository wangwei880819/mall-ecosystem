package com.igou.mall.model.entity;

import lombok.Data;

import java.util.Date;

@Data
public class DeliveryAddress {
    private Long id;
    private Long customerId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
    private String status;
    private Date createTime;
    private Date updateTime;
}