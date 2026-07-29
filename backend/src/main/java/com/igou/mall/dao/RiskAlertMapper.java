package com.igou.mall.dao;

import com.igou.mall.model.entity.RiskAlert;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RiskAlertMapper {
    @Select("SELECT * FROM risk_alert WHERE id = #{id}")
    RiskAlert findById(Long id);

    @Select("SELECT * FROM risk_alert ORDER BY alert_time DESC LIMIT #{offset}, #{limit}")
    List<RiskAlert> findPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT * FROM risk_alert WHERE status = #{status} ORDER BY alert_time DESC")
    List<RiskAlert> findByStatus(String status);

    @Select("SELECT COUNT(*) FROM risk_alert")
    Integer count();

    @Insert("INSERT INTO risk_alert(alert_code, rule_id, rule_code, rule_name, target_type, target_id, target_code, risk_level, alert_action, status, alert_time, handler, handle_time, handle_result, remark, create_time, update_time) VALUES(#{alertCode}, #{ruleId}, #{ruleCode}, #{ruleName}, #{targetType}, #{targetId}, #{targetCode}, #{riskLevel}, #{alertAction}, #{status}, #{alertTime}, #{handler}, #{handleTime}, #{handleResult}, #{remark}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RiskAlert alert);

    @Update("UPDATE risk_alert SET alert_code=#{alertCode}, rule_id=#{ruleId}, rule_code=#{ruleCode}, rule_name=#{ruleName}, target_type=#{targetType}, target_id=#{targetId}, target_code=#{targetCode}, risk_level=#{riskLevel}, alert_action=#{alertAction}, status=#{status}, alert_time=#{alertTime}, handler=#{handler}, handle_time=#{handleTime}, handle_result=#{handleResult}, remark=#{remark}, update_time=#{updateTime} WHERE id=#{id}")
    void update(RiskAlert alert);

    @Delete("DELETE FROM risk_alert WHERE id=#{id}")
    void delete(Long id);
}