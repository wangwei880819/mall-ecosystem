package com.igou.mall.dao;

import com.igou.mall.model.entity.MerchantAuditLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MerchantAuditLogMapper {

    @Insert("INSERT INTO merchant_audit_log (merchant_id, audit_node, action, operator, comment, reject_reason) " +
            "VALUES (#{merchantId}, #{auditNode}, #{action}, #{operator}, #{comment}, #{rejectReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MerchantAuditLog log);

    @Select("SELECT * FROM merchant_audit_log WHERE merchant_id = #{merchantId} ORDER BY create_time ASC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "merchantId", column = "merchant_id"),
        @Result(property = "auditNode", column = "audit_node"),
        @Result(property = "action", column = "action"),
        @Result(property = "operator", column = "operator"),
        @Result(property = "comment", column = "comment"),
        @Result(property = "rejectReason", column = "reject_reason"),
        @Result(property = "createTime", column = "create_time")
    })
    List<MerchantAuditLog> findByMerchantId(Long merchantId);
}
