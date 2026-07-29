package com.igou.mall.dao;

import com.igou.mall.model.entity.BlacklistItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BlacklistMapper {
    @Select("SELECT * FROM blacklist_item ORDER BY create_time DESC")
    List<BlacklistItem> findAll();

    @Insert("INSERT INTO blacklist_item (value, type, list_type, reason, source, operator, create_time, expire_time) VALUES (#{value}, #{type}, #{listType}, #{reason}, #{source}, #{operator}, NOW(), #{expireTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BlacklistItem item);

    @Delete("DELETE FROM blacklist_item WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
