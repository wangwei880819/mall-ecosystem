package com.igou.mall.dao;

import com.igou.mall.model.entity.SystemConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SystemConfigMapper {

    @Select("SELECT * FROM system_config WHERE config_key = #{configKey}")
    SystemConfig findByKey(@Param("configKey") String configKey);

    @Select("SELECT * FROM system_config")
    List<SystemConfig> findAll();

    @Insert("INSERT INTO system_config(config_key, config_value, description) VALUES(#{configKey}, #{configValue}, #{description}) " +
            "ON DUPLICATE KEY UPDATE config_value=#{configValue}, description=#{description}")
    int upsert(SystemConfig config);
}
