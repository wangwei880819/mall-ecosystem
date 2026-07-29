package com.igou.mall.dao;

import com.igou.mall.model.entity.StockChange;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StockChangeMapper {
    @Select("SELECT * FROM stock_change WHERE product_id = #{productId} ORDER BY create_time DESC")
    List<StockChange> findByProductId(@Param("productId") Long productId);

    @Select("SELECT * FROM stock_change WHERE product_id = #{productId} AND change_type = #{changeType} ORDER BY create_time DESC")
    List<StockChange> findByProductIdAndType(@Param("productId") Long productId, @Param("changeType") String changeType);

    @Insert("INSERT INTO stock_change (product_id, change_type, change_amount, before_stock, after_stock, order_code, operator, reason, create_time) VALUES (#{productId}, #{changeType}, #{changeAmount}, #{beforeStock}, #{afterStock}, #{orderCode}, #{operator}, #{reason}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StockChange stockChange);
}