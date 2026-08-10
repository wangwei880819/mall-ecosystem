package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.*;
import com.igou.mall.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rbac")
public class RbacController {

    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SysRoleMapper sysRoleMapper;
    @Autowired private SysMenuMapper sysMenuMapper;
    @Autowired private SysUserRoleMapper sysUserRoleMapper;
    @Autowired private SysRoleMenuMapper sysRoleMenuMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<List<Map<String, Object>>> listUsers(@RequestParam(defaultValue = "0") Integer page,
                                           @RequestParam(defaultValue = "20") Integer size) {
        List<SysUser> users = sysUserMapper.findPage(page * size, size);
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("realName", user.getRealName());
            map.put("phone", user.getPhone());
            map.put("email", user.getEmail());
            map.put("role", user.getRole());
            map.put("status", user.getStatus());
            map.put("platforms", parsePlatforms(user.getPlatforms()));
            map.put("lastLoginTime", user.getLastLoginTime());
            map.put("lastLoginIp", user.getLastLoginIp());
            map.put("createTime", user.getCreateTime());
            map.put("updateTime", user.getUpdateTime());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    private List<Integer> parsePlatforms(String platforms) {
        if (platforms == null || platforms.trim().isEmpty()) {
            return Arrays.asList(1);
        }
        try {
            return Arrays.stream(platforms.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return Arrays.asList(1);
        }
    }

    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> getUserById(@PathVariable Long id) {
        SysUser user = sysUserMapper.findById(id);
        if (user == null) return Result.error("用户不存在");

        List<SysRole> roles = sysRoleMapper.findByUserId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roles);
        result.put("platforms", parsePlatforms(user.getPlatforms()));
        return Result.success(result);
    }

    @PostMapping("/users")
    public Result<String> createUser(@RequestBody Map<String, Object> userData) {
        SysUser user = new SysUser();
        user.setUsername((String) userData.get("username"));
        user.setPassword(passwordEncoder.encode((String) userData.get("password")));
        user.setRealName((String) userData.get("realName"));
        user.setPhone((String) userData.get("phone"));
        user.setEmail((String) userData.get("email"));
        user.setRole("USER");
        user.setStatus("ACTIVE");

        // 处理可登录平台
        Object platformsObj = userData.get("platforms");
        if (platformsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Integer> platforms = (List<Integer>) platformsObj;
            user.setPlatforms(platforms.stream().map(String::valueOf).collect(Collectors.joining(",")));
        } else if (platformsObj instanceof String) {
            user.setPlatforms((String) platformsObj);
        }
        sysUserMapper.insert(user);

        List<?> rawRoleIds = (List<?>) userData.get("roleIds");
        if (rawRoleIds != null) {
            List<Long> roleIds = rawRoleIds.stream()
                    .map(o -> Long.valueOf(o.toString()))
                    .collect(Collectors.toList());
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
        return Result.success("创建成功");
    }

    @PutMapping("/users/{id}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        SysUser user = sysUserMapper.findById(id);
        if (user == null) return Result.error("用户不存在");

        if (userData.containsKey("username")) user.setUsername((String) userData.get("username"));
        if (userData.containsKey("realName")) user.setRealName((String) userData.get("realName"));
        if (userData.containsKey("phone")) user.setPhone((String) userData.get("phone"));
        if (userData.containsKey("email")) user.setEmail((String) userData.get("email"));
        if (userData.containsKey("role")) user.setRole((String) userData.get("role"));
        if (userData.containsKey("status")) user.setStatus((String) userData.get("status"));

        // 处理可登录平台
        if (userData.containsKey("platforms")) {
            Object platformsObj = userData.get("platforms");
            if (platformsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Integer> platforms = (List<Integer>) platformsObj;
                user.setPlatforms(platforms.stream().map(String::valueOf).collect(Collectors.joining(",")));
            } else if (platformsObj instanceof String) {
                user.setPlatforms((String) platformsObj);
            }
        }
        sysUserMapper.update(user);

        if (userData.containsKey("roleIds")) {
            sysUserRoleMapper.deleteByUserId(id);
            List<?> rawRoleIds = (List<?>) userData.get("roleIds");
            List<Long> roleIds = rawRoleIds.stream()
                    .map(o -> Long.valueOf(o.toString()))
                    .collect(Collectors.toList());
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }
        return Result.success("更新成功");
    }

    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        sysUserRoleMapper.deleteByUserId(id);
        sysUserMapper.delete(id);
        return Result.success("删除成功");
    }

    // ==================== 角色管理 ====================

    @GetMapping("/roles")
    public Result<List<SysRole>> listRoles() {
        return Result.success(sysRoleMapper.findAll());
    }

    @GetMapping("/roles/{id}")
    public Result<Map<String, Object>> getRoleById(@PathVariable Long id) {
        SysRole role = sysRoleMapper.findById(id);
        if (role == null) return Result.error("角色不存在");

        List<SysMenu> menus = sysMenuMapper.findByRoleId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        result.put("menus", menus);
        return Result.success(result);
    }

    @PostMapping("/roles")
    public Result<String> createRole(@RequestBody Map<String, Object> roleData) {
        SysRole role = new SysRole();
        role.setRoleCode((String) roleData.get("roleCode"));
        role.setRoleName((String) roleData.get("roleName"));
        role.setRoleDesc((String) roleData.get("roleDesc"));
        role.setDataScope((String) roleData.getOrDefault("dataScope", "ALL"));
        role.setSortOrder((Integer) roleData.getOrDefault("sortOrder", 0));
        role.setStatus("ACTIVE");
        sysRoleMapper.insert(role);

        List<?> rawMenuIds = (List<?>) roleData.get("menuIds");
        if (rawMenuIds != null) {
            List<Long> menuIds = rawMenuIds.stream()
                    .map(o -> Long.valueOf(o.toString()))
                    .collect(Collectors.toList());
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                sysRoleMenuMapper.insert(rm);
            }
        }
        return Result.success("创建成功");
    }

    @PutMapping("/roles/{id}")
    public Result<String> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> roleData) {
        SysRole role = sysRoleMapper.findById(id);
        if (role == null) return Result.error("角色不存在");

        if (roleData.containsKey("roleName")) role.setRoleName((String) roleData.get("roleName"));
        if (roleData.containsKey("roleDesc")) role.setRoleDesc((String) roleData.get("roleDesc"));
        if (roleData.containsKey("dataScope")) role.setDataScope((String) roleData.get("dataScope"));
        if (roleData.containsKey("sortOrder")) role.setSortOrder((Integer) roleData.get("sortOrder"));
        if (roleData.containsKey("status")) role.setStatus((String) roleData.get("status"));
        sysRoleMapper.update(role);

        if (roleData.containsKey("menuIds")) {
            sysRoleMenuMapper.deleteByRoleId(id);
            List<?> rawIds = (List<?>) roleData.get("menuIds");
            List<Long> menuIds = rawIds.stream()
                    .map(o -> Long.valueOf(o.toString()))
                    .collect(Collectors.toList());
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(menuId);
                sysRoleMenuMapper.insert(rm);
            }
        }
        return Result.success("更新成功");
    }

    @DeleteMapping("/roles/{id}")
    public Result<String> deleteRole(@PathVariable Long id) {
        sysRoleMenuMapper.deleteByRoleId(id);
        sysUserRoleMapper.deleteByRoleId(id);
        sysRoleMapper.delete(id);
        return Result.success("删除成功");
    }

    // ==================== 菜单管理 ====================

    @GetMapping("/menus")
    public Result<List<SysMenu>> listMenus() {
        return Result.success(sysMenuMapper.findAll());
    }

    @GetMapping("/menus/tree")
    public Result<List<SysMenu>> getMenuTree() {
        List<SysMenu> menus = sysMenuMapper.findAll();
        return Result.success(buildMenuTree(menus));
    }

    @GetMapping("/menus/{id}")
    public Result<SysMenu> getMenuById(@PathVariable Long id) {
        return Result.success(sysMenuMapper.findById(id));
    }

    @PostMapping("/menus")
    public Result<String> createMenu(@RequestBody SysMenu menu) {
        menu.setStatus("ACTIVE");
        sysMenuMapper.insert(menu);
        return Result.success("创建成功");
    }

    @PutMapping("/menus/{id}")
    public Result<String> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        sysMenuMapper.update(menu);
        return Result.success("更新成功");
    }

    @DeleteMapping("/menus/{id}")
    public Result<String> deleteMenu(@PathVariable Long id) {
        sysRoleMenuMapper.deleteByMenuId(id);
        sysMenuMapper.delete(id);
        return Result.success("删除成功");
    }

    // ==================== 动态菜单（根据用户角色） ====================

    @GetMapping("/menus/user/{userId}")
    public Result<List<SysMenu>> getUserMenus(@PathVariable Long userId) {
        List<SysMenu> menus = sysMenuMapper.findByUserId(userId);
        List<SysMenu> tree = buildMenuTree(menus);
        return Result.success(tree);
    }

    // ==================== 辅助方法 ====================

    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        Map<Long, SysMenu> menuMap = menus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m));

        List<SysMenu> rootMenus = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootMenus.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }

        rootMenus.sort(Comparator.comparing(SysMenu::getSortOrder));
        for (SysMenu menu : rootMenus) {
            menu.getChildren().sort(Comparator.comparing(SysMenu::getSortOrder));
        }
        return rootMenus;
    }
}