package com.igou.mall.dao;

import com.igou.mall.model.entity.DeliveryAddress;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DeliveryAddressMapper {
    @Select("SELECT * FROM delivery_address WHERE customer_id = #{customerId} ORDER BY is_default DESC, create_time DESC")
    List<DeliveryAddress> findByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT * FROM delivery_address WHERE id = #{id}")
    DeliveryAddress findById(@Param("id") Long id);

    @Select("SELECT * FROM delivery_address WHERE customer_id = #{customerId} AND is_default = 1 LIMIT 1")
    DeliveryAddress findDefaultByCustomerId(@Param("customerId") Long customerId);

    @Insert("INSERT INTO delivery_address (customer_id, receiver_name, receiver_phone, province, city, district, address, is_default, remark, create_time, update_time) VALUES (#{customerId}, #{receiverName}, #{receiverPhone}, #{province}, #{city}, #{district}, #{address}, #{isDefault}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DeliveryAddress deliveryAddress);

    @Update("UPDATE delivery_address SET customer_id=#{customerId}, receiver_name=#{receiverName}, receiver_phone=#{receiverPhone}, province=#{province}, city=#{city}, district=#{district}, address=#{address}, is_default=#{isDefault}, remark=#{remark}, update_time=NOW() WHERE id=#{id}")
    int update(DeliveryAddress deliveryAddress);

    @Delete("DELETE FROM delivery_address WHERE id = #{id}")
    int delete(@Param("id") Long id);
}