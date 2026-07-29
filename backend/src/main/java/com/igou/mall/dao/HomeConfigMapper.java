package com.igou.mall.dao;

import com.igou.mall.model.entity.HomeConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeConfigMapper {
    @Select("SELECT * FROM home_config")
    List<HomeConfig> findAll();

    @Select("SELECT * FROM home_config WHERE config_key = #{configKey}")
    HomeConfig findByKey(@Param("configKey") String configKey);

    @Insert("INSERT INTO home_config (config_key, config_value, update_time) VALUES (#{configKey}, #{configValue}, NOW()) ON DUPLICATE KEY UPDATE config_value=#{configValue}, update_time=NOW()")
    int upsert(HomeConfig config);
}
