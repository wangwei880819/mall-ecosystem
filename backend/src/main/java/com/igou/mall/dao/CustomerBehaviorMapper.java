package com.igou.mall.dao;

import com.igou.mall.model.entity.CustomerBehavior;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CustomerBehaviorMapper {
    @Select("SELECT * FROM customer_behavior WHERE customer_id = #{customerId} ORDER BY behavior_time DESC")
    List<CustomerBehavior> findByCustomerId(@Param("customerId") Long customerId);

    @Select("SELECT * FROM customer_behavior WHERE customer_id = #{customerId} AND behavior_type = #{behaviorType} ORDER BY behavior_time DESC")
    List<CustomerBehavior> findByCustomerIdAndType(@Param("customerId") Long customerId, @Param("behaviorType") String behaviorType);

    @Insert("INSERT INTO customer_behavior (customer_id, behavior_type, behavior_data, behavior_time, create_time) VALUES (#{customerId}, #{behaviorType}, #{behaviorData}, #{behaviorTime}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CustomerBehavior customerBehavior);
}