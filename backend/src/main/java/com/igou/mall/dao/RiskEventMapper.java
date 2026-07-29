package com.igou.mall.dao;

import com.igou.mall.model.entity.RiskEvent;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RiskEventMapper {
    @Select("SELECT * FROM risk_event ORDER BY create_time DESC")
    List<RiskEvent> findAll();

    @Select("SELECT * FROM risk_event WHERE id = #{id}")
    RiskEvent findById(@Param("id") Long id);

    @Insert("INSERT INTO risk_event (event_type, target, risk_level, score, hit_rule, status, source, detail, create_time, update_time) VALUES (#{eventType}, #{target}, #{riskLevel}, #{score}, #{hitRule}, #{status}, #{source}, #{detail}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RiskEvent event);

    @Update("UPDATE risk_event SET status=#{status}, update_time=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
