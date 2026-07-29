package com.igou.mall.dao;

import com.igou.mall.model.entity.SysRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    SysRole findById(Long id);

    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole findByCode(String roleCode);

    @Select("SELECT * FROM sys_role WHERE status = 'ACTIVE' ORDER BY sort_order")
    List<SysRole> findAll();

    @Select("SELECT r.* FROM sys_role r JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<SysRole> findByUserId(Long userId);

    @Insert("INSERT INTO sys_role (role_code, role_name, role_desc, data_scope, sort_order, status, create_time, update_time) VALUES (#{roleCode}, #{roleName}, #{roleDesc}, #{dataScope}, #{sortOrder}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRole role);

    @Update("UPDATE sys_role SET role_code=#{roleCode}, role_name=#{roleName}, role_desc=#{roleDesc}, data_scope=#{dataScope}, sort_order=#{sortOrder}, status=#{status}, update_time=NOW() WHERE id=#{id}")
    int update(SysRole role);

    @Delete("DELETE FROM sys_role WHERE id=#{id}")
    int delete(Long id);

    @Select("SELECT COUNT(*) FROM sys_role")
    int count();
}