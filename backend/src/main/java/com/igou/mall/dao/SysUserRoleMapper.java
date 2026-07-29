package com.igou.mall.dao;

import com.igou.mall.model.entity.SysUserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {
    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId}")
    List<SysUserRole> findByUserId(Long userId);

    @Select("SELECT * FROM sys_user_role WHERE role_id = #{roleId}")
    List<SysUserRole> findByRoleId(Long roleId);

    @Insert("INSERT INTO sys_user_role (user_id, role_id, create_time) VALUES (#{userId}, #{roleId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUserRole userRole);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
}