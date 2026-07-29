package com.igou.mall.dao;

import com.igou.mall.model.entity.AuditRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AuditRecordMapper {
    @Select("SELECT * FROM audit_record ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<AuditRecord> findAll(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT * FROM audit_record WHERE audit_type = #{type} ORDER BY create_time DESC")
    List<AuditRecord> findByType(@Param("type") String type);

    @Select("SELECT * FROM audit_record WHERE risk_level = #{level} AND status = 'PENDING'")
    List<AuditRecord> findPendingByRisk(@Param("level") String level);

    @Insert("INSERT INTO audit_record(audit_code, audit_type, target_code, merchant_id, risk_type, " +
            "risk_level, amount, description, status) VALUES(#{auditCode}, #{auditType}, #{targetCode}, " +
            "#{merchantId}, #{riskType}, #{riskLevel}, #{amount}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditRecord record);

    @Update("UPDATE audit_record SET status = #{status}, handler = #{handler}, handle_time = NOW(), " +
            "handle_result = #{result} WHERE id = #{id}")
    int resolve(@Param("id") Long id, @Param("status") String status, @Param("handler") String handler, @Param("result") String result);

    @Select("SELECT COUNT(*) FROM audit_record WHERE risk_level = 'HIGH' AND status = 'PENDING'")
    int highRiskPendingCount();
}
