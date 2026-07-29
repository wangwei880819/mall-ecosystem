package com.igou.mall.dao;

import com.igou.mall.model.entity.SettlementDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SettlementDetailMapper {
    @Select("SELECT * FROM settlement_detail WHERE settle_id = #{settleId}")
    List<SettlementDetail> findBySettleId(@Param("settleId") Long settleId);

    @Insert("INSERT INTO settlement_detail (settle_id, order_code, order_amount, commission_rate, commission_amount, ai_dou_amount, merchant_amount, create_time) VALUES (#{settleId}, #{orderCode}, #{orderAmount}, #{commissionRate}, #{commissionAmount}, #{aiDouAmount}, #{merchantAmount}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SettlementDetail settlementDetail);

    @Delete("DELETE FROM settlement_detail WHERE settle_id = #{settleId}")
    int deleteBySettleId(@Param("settleId") Long settleId);
}