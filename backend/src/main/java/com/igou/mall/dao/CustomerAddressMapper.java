package com.igou.mall.dao;

import com.igou.mall.model.entity.CustomerAddress;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerAddressMapper {
    @Select("SELECT * FROM delivery_address WHERE id = #{id}")
    CustomerAddress findById(Long id);

    @Select("SELECT * FROM delivery_address ORDER BY is_default DESC, create_time DESC")
    List<CustomerAddress> findAll();

    @Select("SELECT * FROM delivery_address WHERE customer_id = #{customerId} ORDER BY is_default DESC, create_time DESC")
    List<CustomerAddress> findByCustomerId(Long customerId);

    @Insert("INSERT INTO delivery_address(customer_id, receiver_name, receiver_phone, province, city, district, detail_address, is_default, create_time, update_time) VALUES(#{customerId}, #{receiverName}, #{receiverPhone}, #{province}, #{city}, #{district}, #{detailAddress}, #{isDefault}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CustomerAddress address);

    @Update("UPDATE delivery_address SET receiver_name=#{receiverName}, receiver_phone=#{receiverPhone}, province=#{province}, city=#{city}, district=#{district}, detail_address=#{detailAddress}, is_default=#{isDefault}, update_time=NOW() WHERE id=#{id}")
    void update(CustomerAddress address);

    @Delete("DELETE FROM delivery_address WHERE id=#{id}")
    void delete(Long id);
}
