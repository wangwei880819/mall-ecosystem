package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.SysUserMapper;
import com.igou.mall.dao.ApiConfigMapper;
import com.igou.mall.dao.SysMenuMapper;
import com.igou.mall.dao.SsoPlatformMapper;
import com.igou.mall.model.entity.SysUser;
import com.igou.mall.model.entity.ApiConfig;
import com.igou.mall.model.entity.SsoPlatform;
import com.igou.mall.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ApiConfigMapper apiConfigMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SsoPlatformMapper ssoPlatformMapper;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        if (!user.getStatus().equals("ACTIVE")) {
            return Result.error(403, "账号已被禁用");
        }

        // 密码验证：兼容BCrypt和明文密码（过渡期）
        String storedPassword = user.getPassword();
        if (storedPassword.startsWith("$2a$")) {
            if (!passwordEncoder.matches(password, storedPassword)) {
                return Result.error(401, "密码错误");
            }
        } else {
            if (!password.equals(storedPassword)) {
                return Result.error(401, "密码错误");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtService.generateToken(user.getUsername(), user.getRole(), user.getId()));
        result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName(),
                "role", user.getRole()
        ));

        return Result.success(result);
    }

    @GetMapping("/menus")
    public Result<List<Map<String, Object>>> getUserMenus(@RequestParam Long userId) {
        List<com.igou.mall.model.entity.SysMenu> menus = sysMenuMapper.findByUserId(userId);
        List<Map<String, Object>> tree = buildMenuTree(menus);
        return Result.success(tree);
    }

    private List<Map<String, Object>> buildMenuTree(List<com.igou.mall.model.entity.SysMenu> menus) {
        Map<Long, Map<String, Object>> menuMap = new HashMap<>();
        List<Map<String, Object>> rootMenus = new ArrayList<>();

        for (com.igou.mall.model.entity.SysMenu menu : menus) {
            if ("BUTTON".equals(menu.getMenuType())) continue;
            
            Map<String, Object> menuNode = new HashMap<>();
            menuNode.put("id", menu.getId());
            menuNode.put("name", menu.getMenuName());
            menuNode.put("path", menu.getPath());
            menuNode.put("icon", menu.getIcon());
            menuNode.put("type", menu.getMenuType());
            menuNode.put("children", new ArrayList<>());
            menuMap.put(menu.getId(), menuNode);

            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootMenus.add(menuNode);
            } else {
                Map<String, Object> parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(menuNode);
                }
            }
        }

        rootMenus.sort(Comparator.comparing(m -> (Integer) ((com.igou.mall.model.entity.SysMenu) menus.stream()
                .filter(mm -> mm.getId().equals(m.get("id"))).findFirst().orElse(new com.igou.mall.model.entity.SysMenu())).getSortOrder()));
        
        return rootMenus;
    }

    @GetMapping("/sso/platforms")
    public Result<List<SsoPlatform>> getSsoPlatforms() {
        return Result.success(ssoPlatformMapper.findAll());
    }

    @GetMapping("/sso/platforms/active")
    public Result<List<SsoPlatform>> getActivePlatforms() {
        return Result.success(ssoPlatformMapper.findActive());
    }

    @PostMapping("/sso/platforms")
    public Result<SsoPlatform> addPlatform(@RequestBody SsoPlatform platform) {
        if (platform.getStatus() == null) {
            platform.setStatus("ACTIVE");
        }
        ssoPlatformMapper.insert(platform);
        return Result.success(platform);
    }

    @PutMapping("/sso/platforms/{id}")
    public Result<String> updatePlatform(@PathVariable Long id, @RequestBody SsoPlatform platform) {
        platform.setId(id);
        ssoPlatformMapper.update(platform);
        return Result.success("更新成功");
    }

    @PutMapping("/sso/platforms/{id}/status")
    public Result<String> updatePlatformStatus(@PathVariable Long id, @RequestParam String status) {
        ssoPlatformMapper.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/sso/platforms/{id}")
    public Result<String> deletePlatform(@PathVariable Long id) {
        ssoPlatformMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/rbac/user/platforms")
    public Result<List<Integer>> getUserPlatforms(@RequestParam Long userId) {
        SysUser user = sysUserMapper.findById(userId);
        if (user == null || user.getPlatforms() == null || user.getPlatforms().trim().isEmpty()) {
            return Result.success(Arrays.asList(1));
        }
        List<Integer> platforms = Arrays.stream(user.getPlatforms().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(platforms);
    }

    @GetMapping("/sso/configs")
    public Result<List<ApiConfig>> getApiConfigs() {
        return Result.success(apiConfigMapper.findAll());
    }

    @PostMapping("/sso/configs")
    public Result<String> addApiConfig(@RequestBody ApiConfig config) {
        apiConfigMapper.insert(config);
        return Result.success("添加成功");
    }

    @PutMapping("/sso/configs/{id}/status")
    public Result<String> updateConfigStatus(@PathVariable Long id, @RequestParam String status) {
        apiConfigMapper.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
