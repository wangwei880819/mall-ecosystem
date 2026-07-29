package com.igou.mall.dao;

import com.igou.mall.model.entity.SysRoleMenu;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper {
    @Select("SELECT * FROM sys_role_menu WHERE role_id = #{roleId}")
    List<SysRoleMenu> findByRoleId(Long roleId);

    @Select("SELECT * FROM sys_role_menu WHERE menu_id = #{menuId}")
    List<SysRoleMenu> findByMenuId(Long menuId);

    @Insert("INSERT INTO sys_role_menu (role_id, menu_id, create_time) VALUES (#{roleId}, #{menuId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysRoleMenu roleMenu);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);

    @Delete("DELETE FROM sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(Long menuId);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId} AND menu_id = #{menuId}")
    int deleteByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}