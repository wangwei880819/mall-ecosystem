package com.igou.mall.dao;

import com.igou.mall.model.entity.DisposalConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DisposalMapper {
    @Select("SELECT * FROM disposal_config ORDER BY create_time DESC")
    List<DisposalConfig> findAll();

    @Select("SELECT * FROM disposal_config WHERE id = #{id}")
    DisposalConfig findById(@Param("id") Long id);

    @Insert("INSERT INTO disposal_config (name, type, trigger_rule, risk_level, duration, status, exec_count, description, create_time, update_time) VALUES (#{name}, #{type}, #{triggerRule}, #{riskLevel}, #{duration}, #{status}, #{execCount}, #{description}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DisposalConfig config);

    @Update("UPDATE disposal_config SET name=#{name}, type=#{type}, trigger_rule=#{triggerRule}, risk_level=#{riskLevel}, duration=#{duration}, status=#{status}, exec_count=#{execCount}, description=#{description}, update_time=NOW() WHERE id=#{id}")
    int update(DisposalConfig config);
}
