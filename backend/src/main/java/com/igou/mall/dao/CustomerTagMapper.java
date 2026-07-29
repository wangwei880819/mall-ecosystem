package com.igou.mall.dao;

import com.igou.mall.model.entity.CustomerTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerTagMapper {
    @Select("SELECT * FROM customer_tag WHERE id = #{id}")
    CustomerTag findById(Long id);

    @Select("SELECT * FROM customer_tag WHERE customer_id = #{customerId}")
    List<CustomerTag> findByCustomerId(Long customerId);

    @Insert("INSERT INTO customer_tag(customer_id, tag_name, tag_type, create_time) VALUES(#{customerId}, #{tagName}, #{tagType}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CustomerTag tag);

    @Delete("DELETE FROM customer_tag WHERE id=#{id}")
    void delete(Long id);

    @Delete("DELETE FROM customer_tag WHERE customer_id=#{customerId} AND tag_name=#{tagName}")
    void deleteByCustomerIdAndTagName(@Param("customerId") Long customerId, @Param("tagName") String tagName);
}