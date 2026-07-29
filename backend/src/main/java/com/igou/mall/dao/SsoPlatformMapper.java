package com.igou.mall.dao;

import com.igou.mall.model.entity.SsoPlatform;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SsoPlatformMapper {
    @Select("SELECT id, name, system_code, auth_type, icon, url, status, create_time FROM sso_platform ORDER BY id ASC")
    @Results({
        @Result(property = "systemCode", column = "system_code"),
        @Result(property = "authType", column = "auth_type"),
        @Result(property = "createTime", column = "create_time")
    })
    List<SsoPlatform> findAll();

    @Select("SELECT id, name, system_code, auth_type, icon, url, status, create_time FROM sso_platform WHERE id = #{id}")
    @Results({
        @Result(property = "systemCode", column = "system_code"),
        @Result(property = "authType", column = "auth_type"),
        @Result(property = "createTime", column = "create_time")
    })
    SsoPlatform findById(@Param("id") Long id);

    @Insert("INSERT INTO sso_platform(name, system_code, auth_type, icon, url, status) " +
            "VALUES(#{name}, #{systemCode}, #{authType}, #{icon}, #{url}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SsoPlatform platform);

    @Update("UPDATE sso_platform SET name=#{name}, system_code=#{systemCode}, auth_type=#{authType}, " +
            "icon=#{icon}, url=#{url}, status=#{status} WHERE id=#{id}")
    int update(SsoPlatform platform);

    @Update("UPDATE sso_platform SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM sso_platform WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, name, system_code, auth_type, icon, url, status, create_time FROM sso_platform WHERE status='ACTIVE' ORDER BY id ASC")
    @Results({
        @Result(property = "systemCode", column = "system_code"),
        @Result(property = "authType", column = "auth_type"),
        @Result(property = "createTime", column = "create_time")
    })
    List<SsoPlatform> findActive();
}
