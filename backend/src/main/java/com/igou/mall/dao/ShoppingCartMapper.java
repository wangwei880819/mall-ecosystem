package com.igou.mall.dao;

import com.igou.mall.model.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    @Select("SELECT * FROM shopping_cart WHERE customer_id = #{customerId} ORDER BY create_time DESC")
    List<ShoppingCart> findByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT * FROM shopping_cart WHERE customer_id = #{customerId} AND product_id = #{productId}")
    ShoppingCart findByCustomerIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);

    @Insert("INSERT INTO shopping_cart (customer_id, product_id, product_name, product_image, product_price, quantity, create_time, update_time) VALUES (#{customerId}, #{productId}, #{productName}, #{productImage}, #{productPrice}, #{quantity}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShoppingCart shoppingCart);

    @Update("UPDATE shopping_cart SET customer_id=#{customerId}, product_id=#{productId}, product_name=#{productName}, product_image=#{productImage}, product_price=#{productPrice}, quantity=#{quantity}, update_time=NOW() WHERE id=#{id}")
    int update(ShoppingCart shoppingCart);

    @Delete("DELETE FROM shopping_cart WHERE id = #{id}")
    int delete(@Param("id") Long id);

    @Delete("DELETE FROM shopping_cart WHERE customer_id = #{customerId}")
    int deleteByCustomerId(@Param("customerId") Long customerId);

    @Delete("DELETE FROM shopping_cart WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);
}