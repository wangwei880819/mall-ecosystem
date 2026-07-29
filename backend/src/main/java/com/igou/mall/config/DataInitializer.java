package com.igou.mall.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据初始化器 - 确保RBAC基础数据在应用启动时存在
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("开始检查并初始化RBAC基础数据...");
        try {
            initTables();
            initRoles();
            initMenus();
            initRoleMenus();
            initAdminRole();
            log.info("RBAC基础数据初始化完成");
        } catch (Exception e) {
            log.error("RBAC数据初始化失败: {}", e.getMessage(), e);
        }
    }

    private void initTables() {
        // sys_role 表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "role_code VARCHAR(64) NOT NULL," +
                "role_name VARCHAR(64) NOT NULL," +
                "role_desc VARCHAR(256)," +
                "data_scope VARCHAR(32) DEFAULT 'ALL'," +
                "sort_order INT DEFAULT 0," +
                "status VARCHAR(16) DEFAULT 'ACTIVE'," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY uk_role_code (role_code)" +
                ")");

        // sys_menu 表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_menu (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "parent_id BIGINT DEFAULT 0," +
                "menu_name VARCHAR(64) NOT NULL," +
                "menu_type VARCHAR(16) NOT NULL DEFAULT 'MENU'," +
                "path VARCHAR(256)," +
                "component VARCHAR(256)," +
                "permission VARCHAR(128)," +
                "icon VARCHAR(64)," +
                "sort_order INT DEFAULT 0," +
                "visible INT DEFAULT 1," +
                "keep_alive INT DEFAULT 0," +
                "status VARCHAR(16) DEFAULT 'ACTIVE'," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // sys_user_role 表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user_role (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT NOT NULL," +
                "role_id BIGINT NOT NULL," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // sys_role_menu 表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_role_menu (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "role_id BIGINT NOT NULL," +
                "menu_id BIGINT NOT NULL," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")");
    }

    private void initRoles() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role", Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name, role_desc, data_scope, sort_order, status) VALUES (?, ?, ?, ?, ?, ?)",
                    "SUPER_ADMIN", "超级管理员", "拥有所有权限", "ALL", 1, "ACTIVE");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name, role_desc, data_scope, sort_order, status) VALUES (?, ?, ?, ?, ?, ?)",
                    "OPERATOR", "运营人员", "商品、订单、客户管理", "SELF", 2, "ACTIVE");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name, role_desc, data_scope, sort_order, status) VALUES (?, ?, ?, ?, ?, ?)",
                    "AUDITOR", "审核人员", "商户审核、风险管理", "ALL", 3, "ACTIVE");
            log.info("已初始化角色数据");
        }
    }

    private void initMenus() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu", Integer.class);
        if (count != null && count > 0) {
            // 已有数据，只检查并补充缺失的"商品审核"菜单
            Integer auditCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_menu WHERE menu_name = ? AND path = ?",
                    Integer.class, "商品审核", "/product/audit");
            if (auditCount != null && auditCount == 0) {
                Long parentId = jdbcTemplate.queryForObject(
                        "SELECT id FROM sys_menu WHERE menu_name = ? AND (parent_id = 0 OR parent_id IS NULL) LIMIT 1",
                        Long.class, "商品管理");
                if (parentId != null) {
                    jdbcTemplate.update(
                            "INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            parentId, "商品审核", "MENU", "/product/audit", "✅", 2, 1, 0, "ACTIVE");
                    log.info("已补充缺失的「商品审核」菜单");
                }
            }
            return;
        }

        // 全新环境，插入所有菜单
        log.info("初始化菜单数据...");

        // 一级菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (1, 0, '首页', 'DIRECTORY', '/portal', '🏠', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (2, 0, '商户管理', 'DIRECTORY', '/merchant', '🏢', 2, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (3, 0, '客户管理', 'DIRECTORY', '/customer', '👥', 3, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (4, 0, '商品管理', 'DIRECTORY', '/product', '📦', 4, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (5, 0, '订单管理', 'DIRECTORY', '/order', '📋', 5, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (6, 0, '财务管理', 'DIRECTORY', '/finance', '💰', 6, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (7, 0, '风险管理', 'DIRECTORY', '/risk', '🛡️', 7, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (8, 0, '系统管理', 'DIRECTORY', '/system', '⚙️', 8, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (10, 0, 'AI+应用', 'DIRECTORY', '/ai', '🤖', 10, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (9, 0, 'C端配置', 'DIRECTORY', '/cconfig', '⚙️', 9, 1, 0, 'ACTIVE')");

        // 商户管理子菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (11, 2, '商户列表', 'MENU', '/merchant/list', '📋', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (12, 2, '资质审核', 'MENU', '/merchant/audit', '✅', 2, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (32, 2, '业务复审', 'MENU', '/merchant/business-audit', '📋', 3, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (33, 2, '合规终审', 'MENU', '/merchant/compliance-audit', '🔍', 4, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (34, 2, '合同签署', 'MENU', '/merchant/contract-audit', '📝', 5, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (35, 2, '支付进件', 'MENU', '/merchant/payment-audit', '💳', 6, 1, 0, 'ACTIVE')");

        // 客户管理子菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (13, 3, '客户列表', 'MENU', '/customer/list', '📋', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (14, 3, '客户标签', 'MENU', '/customer/tags', '🏷️', 2, 1, 0, 'ACTIVE')");

        // 商品管理子菜单（含商品审核）
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (16, 4, '商品列表', 'MENU', '/product/list', '📋', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (36, 4, '商品审核', 'MENU', '/product/audit', '✅', 2, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (17, 4, '分类管理', 'MENU', '/product/category', '📂', 3, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (18, 4, '库存管理', 'MENU', '/product/stock', '📦', 4, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (19, 4, '权益引入', 'MENU', '/product/benefit', '🎁', 5, 1, 0, 'ACTIVE')");

        // 订单管理子菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (20, 5, '订单列表', 'MENU', '/order/list', '📋', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (21, 5, '支付管理', 'MENU', '/order/pay', '💳', 2, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (22, 5, '退款管理', 'MENU', '/order/refund', '💰', 3, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (37, 5, '订单评价', 'MENU', '/order/evaluation', '⭐', 4, 1, 0, 'ACTIVE')");

        // 财务管理子菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (23, 6, '结算管理', 'MENU', '/finance/settlement', '📊', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (24, 6, '发票管理', 'MENU', '/finance/invoice', '📄', 2, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (25, 6, '对账管理', 'MENU', '/finance/reconciliation', '🔍', 3, 1, 0, 'ACTIVE')");

        // 风险管理子菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (26, 7, '规则管理', 'MENU', '/risk/rules', '📋', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (27, 7, '风险告警', 'MENU', '/risk/alerts', '🚨', 2, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (28, 7, '交易监控', 'MENU', '/risk/monitor', '📈', 3, 1, 0, 'ACTIVE')");

        // 系统管理子菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (29, 8, '用户管理', 'MENU', '/system/users', '👥', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (30, 8, '角色管理', 'MENU', '/system/roles', '🎭', 2, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (31, 8, '菜单管理', 'MENU', '/system/menus', '📑', 3, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (38, 8, '接入平台', 'MENU', '/system/platforms', '🔗', 4, 1, 0, 'ACTIVE')");

        // C端配置子菜单
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (39, 9, '轮播图管理', 'MENU', '/cconfig/banners', '🎠', 1, 1, 0, 'ACTIVE')");
        jdbcTemplate.update("INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort_order, visible, keep_alive, status) VALUES (40, 9, '首页配置', 'MENU', '/cconfig/home', '🏠', 2, 1, 0, 'ACTIVE')");
        log.info("菜单数据初始化完成");
    }

    private void initRoleMenus() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role_menu", Integer.class);
        if (count != null && count > 0) {
            // 已有角色菜单关联，检查商品审核是否关联了管理员角色
            Integer auditMenuCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_role_menu rm " +
                    "JOIN sys_menu m ON rm.menu_id = m.id " +
                    "WHERE m.menu_name = ? AND m.path = ?",
                    Integer.class, "商品审核", "/product/audit");
            if (auditMenuCount == null || auditMenuCount == 0) {
                // 获取商品审核菜单ID和管理员角色ID
                Long menuId = jdbcTemplate.queryForObject(
                        "SELECT id FROM sys_menu WHERE menu_name = ? AND path = ? LIMIT 1",
                        Long.class, "商品审核", "/product/audit");
                List<Long> roleIds = jdbcTemplate.queryForList(
                        "SELECT id FROM sys_role WHERE role_code = 'SUPER_ADMIN'", Long.class);
                if (menuId != null && !roleIds.isEmpty()) {
                    for (Long roleId : roleIds) {
                        jdbcTemplate.update(
                                "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)",
                                roleId, menuId);
                    }
                    log.info("已为管理员角色授权「商品审核」菜单");
                }
            }
            return;
        }

        // 全新环境，关联所有菜单到超级管理员角色
        Long superAdminRoleId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_role WHERE role_code = 'SUPER_ADMIN' LIMIT 1", Long.class);
        if (superAdminRoleId == null) return;

        List<Long> menuIds = jdbcTemplate.queryForList("SELECT id FROM sys_menu WHERE status = 'ACTIVE'", Long.class);
        for (Long menuId : menuIds) {
            jdbcTemplate.update("INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)",
                    superAdminRoleId, menuId);
        }
        log.info("已初始化角色菜单关联（{}个菜单关联到超级管理员）", menuIds.size());
    }

    private void initAdminRole() {
        // 确保admin用户关联了超级管理员角色
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_role WHERE user_id = 1", Integer.class);
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) " +
                    "SELECT 1, id FROM sys_role WHERE role_code = 'SUPER_ADMIN' LIMIT 1");
            log.info("已初始化admin用户角色关联");
        }
    }
}
