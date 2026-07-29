package com.igou.mall.dao;

import com.igou.mall.model.entity.SysMenu;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysMenuMapper {
    @Select("SELECT * FROM sys_menu WHERE id = #{id}")
    SysMenu findById(Long id);

    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId} AND status = 'ACTIVE' ORDER BY sort_order")
    List<SysMenu> findByParentId(Long parentId);

    @Select("SELECT * FROM sys_menu WHERE status = 'ACTIVE' ORDER BY parent_id, sort_order")
    List<SysMenu> findAll();

    @Select("SELECT m.* FROM sys_menu m JOIN sys_role_menu rm ON m.id = rm.menu_id JOIN sys_user_role ur ON rm.role_id = ur.role_id WHERE ur.user_id = #{userId} AND m.status = 'ACTIVE' ORDER BY m.parent_id, m.sort_order")
    List<SysMenu> findByUserId(Long userId);

    @Select("SELECT m.* FROM sys_menu m JOIN sys_role_menu rm ON m.id = rm.menu_id WHERE rm.role_id = #{roleId} AND m.status = 'ACTIVE' ORDER BY m.parent_id, m.sort_order")
    List<SysMenu> findByRoleId(Long roleId);

    @Insert("INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, keep_alive, status, create_time, update_time) VALUES (#{parentId}, #{menuName}, #{menuType}, #{path}, #{component}, #{permission}, #{icon}, #{sortOrder}, #{visible}, #{keepAlive}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysMenu menu);

    @Update("UPDATE sys_menu SET parent_id=#{parentId}, menu_name=#{menuName}, menu_type=#{menuType}, path=#{path}, component=#{component}, permission=#{permission}, icon=#{icon}, sort_order=#{sortOrder}, visible=#{visible}, keep_alive=#{keepAlive}, status=#{status}, update_time=NOW() WHERE id=#{id}")
    int update(SysMenu menu);

    @Delete("DELETE FROM sys_menu WHERE id=#{id}")
    int delete(Long id);

    @Select("SELECT COUNT(*) FROM sys_menu WHERE parent_id = #{parentId}")
    int countByParentId(Long parentId);

    @Select("SELECT COUNT(*) FROM sys_menu")
    int count();
}