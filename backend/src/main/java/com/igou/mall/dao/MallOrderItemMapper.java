package com.igou.mall.dao;

import com.igou.mall.model.entity.MallOrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MallOrderItemMapper {
    @Select("SELECT * FROM mall_order_item WHERE id = #{id}")
    MallOrderItem findById(Long id);

    @Select("SELECT * FROM mall_order_item WHERE order_id = #{orderId} ORDER BY create_time")
    List<MallOrderItem> findByOrderId(Long orderId);

    @Insert("INSERT INTO mall_order_item(order_id, product_id, product_name, product_image, price, quantity, item_amount, create_time) VALUES(#{orderId}, #{productId}, #{productName}, #{productImage}, #{price}, #{quantity}, #{itemAmount}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MallOrderItem item);

    @Update("UPDATE mall_order_item SET quantity=#{quantity}, item_amount=#{itemAmount} WHERE id=#{id}")
    int update(MallOrderItem item);

    @Delete("DELETE FROM mall_order_item WHERE id=#{id}")
    int delete(Long id);

    @Delete("DELETE FROM mall_order_item WHERE order_id=#{orderId}")
    int deleteByOrderId(Long orderId);
}
