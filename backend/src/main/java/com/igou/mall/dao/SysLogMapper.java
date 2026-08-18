package com.igou.mall.dao;

import com.igou.mall.model.entity.SysLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysLogMapper {

    @Insert("INSERT INTO sys_log(log_type, operator, operator_id, operation, module, target_type, target_id, " +
            "detail, ip_address, user_agent, result, error_message, request_uri, request_method, request_params, " +
            "response_body, cost_time, create_time) " +
            "VALUES(#{logType}, #{operator}, #{operatorId}, #{operation}, #{module}, #{targetType}, #{targetId}, " +
            "#{detail}, #{ipAddress}, #{userAgent}, #{result}, #{errorMessage}, #{requestUri}, #{requestMethod}, " +
            "#{requestParams}, #{responseBody}, #{costTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SysLog log);

    @Select("<script>SELECT * FROM sys_log WHERE 1=1 " +
            "<if test='logType != null and logType != \"\"'> AND log_type = #{logType}</if> " +
            "<if test='operator != null and operator != \"\"'> AND operator LIKE CONCAT('%', #{operator}, '%')</if> " +
            "<if test='module != null and module != \"\"'> AND module = #{module}</if> " +
            "<if test='result != null and result != \"\"'> AND result = #{result}</if> " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if> " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}</script>")
    List<SysLog> findPage(@Param("offset") int offset, @Param("limit") int limit,
                          @Param("logType") String logType, @Param("operator") String operator,
                          @Param("module") String module, @Param("result") String result,
                          @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("<script>SELECT COUNT(*) FROM sys_log WHERE 1=1 " +
            "<if test='logType != null and logType != \"\"'> AND log_type = #{logType}</if> " +
            "<if test='operator != null and operator != \"\"'> AND operator LIKE CONCAT('%', #{operator}, '%')</if> " +
            "<if test='module != null and module != \"\"'> AND module = #{module}</if> " +
            "<if test='result != null and result != \"\"'> AND result = #{result}</if> " +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime}</if> " +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if></script>")
    int count(@Param("logType") String logType, @Param("operator") String operator,
              @Param("module") String module, @Param("result") String result,
              @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT * FROM sys_log WHERE id = #{id}")
    SysLog findById(Long id);
}