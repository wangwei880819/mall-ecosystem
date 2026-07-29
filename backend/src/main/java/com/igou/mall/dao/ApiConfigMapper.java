package com.igou.mall.dao;

import com.igou.mall.model.entity.ApiConfig;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ApiConfigMapper {
    @Select("SELECT * FROM api_config ORDER BY create_time DESC")
    List<ApiConfig> findAll();

    @Select("SELECT * FROM api_config WHERE id = #{id}")
    ApiConfig findById(@Param("id") Long id);

    @Select("SELECT * FROM api_config WHERE target_system = #{targetSystem}")
    List<ApiConfig> findByTargetSystem(@Param("targetSystem") String targetSystem);

    @Insert("INSERT INTO api_config(api_name, api_path, target_system, protocol, auth_type, rate_limit, timeout_ms, status) " +
            "VALUES(#{apiName}, #{apiPath}, #{targetSystem}, #{protocol}, #{authType}, #{rateLimit}, #{timeoutMs}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApiConfig config);

    @Update("UPDATE api_config SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
