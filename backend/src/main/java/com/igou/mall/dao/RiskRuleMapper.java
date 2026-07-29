package com.igou.mall.dao;

import com.igou.mall.model.entity.RiskRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RiskRuleMapper {
    @Select("SELECT * FROM risk_check_rule ORDER BY priority ASC")
    List<RiskRule> findAll();

    @Select("SELECT * FROM risk_check_rule WHERE id = #{id}")
    RiskRule findById(@Param("id") Long id);

    @Insert("INSERT INTO risk_check_rule (name, type, scene, priority, rule_condition, action, hit_count, active, description, create_time, update_time) VALUES (#{name}, #{type}, #{scene}, #{priority}, #{ruleCondition}, #{action}, #{hitCount}, #{active}, #{description}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RiskRule rule);

    @Update("UPDATE risk_check_rule SET name=#{name}, type=#{type}, scene=#{scene}, priority=#{priority}, rule_condition=#{ruleCondition}, action=#{action}, hit_count=#{hitCount}, active=#{active}, description=#{description}, update_time=NOW() WHERE id=#{id}")
    int update(RiskRule rule);

    @Delete("DELETE FROM risk_check_rule WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Update("UPDATE risk_check_rule SET active = NOT active, update_time=NOW() WHERE id=#{id}")
    int toggleStatus(@Param("id") Long id);
}
