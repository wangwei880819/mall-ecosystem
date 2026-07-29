package com.igou.mall.dao;

import com.igou.mall.model.entity.SysUser;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SysUserMapper {
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser findByUsername(@Param("username") String username);

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    SysUser findById(@Param("id") Long id);

    @Select("SELECT * FROM sys_user ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<SysUser> findAll(@Param("offset") int offset, @Param("limit") int limit);

    @Insert("INSERT INTO sys_user(username, password, real_name, phone, email, role, status, platforms) " +
            "VALUES(#{username}, #{password}, #{realName}, #{phone}, #{email}, #{role}, #{status}, #{platforms})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET last_login_time = NOW(), last_login_ip = #{ip} WHERE id = #{id}")
    int updateLoginTime(@Param("id") Long id, @Param("ip") String ip);

    @Select("SELECT * FROM sys_user ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<SysUser> findPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    @Update("UPDATE sys_user SET username=#{username}, password=#{password}, real_name=#{realName}, phone=#{phone}, email=#{email}, role=#{role}, status=#{status}, platforms=#{platforms}, update_time=NOW() WHERE id=#{id}")
    int update(SysUser user);

    @Delete("DELETE FROM sys_user WHERE id=#{id}")
    int delete(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_user")
    int count();
}
