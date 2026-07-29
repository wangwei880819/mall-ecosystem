-- ================================================================
-- RBAC权限管理系统表结构
-- 参考RuoYi框架设计
-- ================================================================

USE igou_mall;

-- ================================================================
-- 1. 角色表
-- ================================================================
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_desc VARCHAR(512) COMMENT '角色描述',
    data_scope VARCHAR(32) DEFAULT 'ALL' COMMENT '数据范围：ALL/DEPT/SELF',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ================================================================
-- 2. 菜单表
-- ================================================================
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(128) NOT NULL COMMENT '菜单名称',
    menu_type VARCHAR(16) NOT NULL COMMENT '菜单类型：DIRECTORY/MENU/BUTTON',
    path VARCHAR(256) COMMENT '路由路径',
    component VARCHAR(256) COMMENT '组件路径',
    permission VARCHAR(128) COMMENT '权限标识',
    icon VARCHAR(64) COMMENT '菜单图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT(1) DEFAULT 1 COMMENT '是否可见：0-不可见，1-可见',
    keep_alive TINYINT(1) DEFAULT 0 COMMENT '是否缓存：0-不缓存，1-缓存',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_parent_id (parent_id),
    KEY idx_menu_type (menu_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ================================================================
-- 3. 用户角色关联表
-- ================================================================
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ================================================================
-- 4. 角色菜单关联表
-- ================================================================
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ================================================================
-- 初始化数据
-- ================================================================
INSERT INTO sys_role (role_code, role_name, role_desc, data_scope, sort_order, status) VALUES
('SUPER_ADMIN', '超级管理员', '系统最高权限，拥有所有功能权限', 'ALL', 1, 'ACTIVE'),
('SYS_ADMIN', '系统管理员', '系统管理相关权限', 'ALL', 2, 'ACTIVE'),
('OPERATOR', '运营人员', '日常运营操作权限', 'DEPT', 3, 'ACTIVE'),
('AUDITOR', '审核人员', '资质审核、风险审核权限', 'DEPT', 4, 'ACTIVE'),
('FINANCE', '财务人员', '财务结算、对账权限', 'DEPT', 5, 'ACTIVE');

INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, visible, keep_alive, status) VALUES
-- 一级菜单
(0, '首页', 'DIRECTORY', '/portal', '', '', '🏠', 1, 1, 0, 'ACTIVE'),
(0, '商户管理', 'DIRECTORY', '/merchant', '', '', '🏢', 2, 1, 0, 'ACTIVE'),
(0, '客户管理', 'DIRECTORY', '/customer', '', '', '👥', 3, 1, 0, 'ACTIVE'),
(0, '商品管理', 'DIRECTORY', '/product', '', '', '📦', 4, 1, 0, 'ACTIVE'),
(0, '订单管理', 'DIRECTORY', '/order', '', '', '📋', 5, 1, 0, 'ACTIVE'),
(0, '财务管理', 'DIRECTORY', '/finance', '', '', '💰', 6, 1, 0, 'ACTIVE'),
(0, '风险管理', 'DIRECTORY', '/risk', '', '', '🛡️', 7, 1, 0, 'ACTIVE'),
(0, '系统管理', 'DIRECTORY', '/system', '', '', '⚙️', 8, 1, 0, 'ACTIVE'),
(5, '订单评价', 'MENU', '/order/evaluation', '../views/service/Index.vue', '', '⭐', 4, 1, 0, 'ACTIVE'),
(0, 'AI+应用', 'DIRECTORY', '/ai', '', '', '🤖', 10, 1, 0, 'ACTIVE'),

-- 商户管理子菜单
(2, '商户列表', 'MENU', '/merchant/list', '../views/onboarding/Merchant.vue', 'merchant:list', '📋', 1, 1, 0, 'ACTIVE'),
(2, '资质审核', 'MENU', '/merchant/audit', '../views/onboarding/MerchantAudit.vue', 'merchant:audit', '✅', 2, 1, 0, 'ACTIVE'),
(2, '业务复审', 'MENU', '/merchant/business-audit', '../views/onboarding/BusinessAudit.vue', 'merchant:business-audit', '📋', 3, 1, 0, 'ACTIVE'),
(2, '合规终审', 'MENU', '/merchant/compliance-audit', '../views/onboarding/ComplianceAudit.vue', 'merchant:compliance-audit', '🔍', 4, 1, 0, 'ACTIVE'),
(2, '合同签署', 'MENU', '/merchant/contract-audit', '../views/onboarding/ContractAudit.vue', 'merchant:contract-audit', '📝', 5, 1, 0, 'ACTIVE'),
(2, '支付进件', 'MENU', '/merchant/payment-audit', '../views/onboarding/PaymentAudit.vue', 'merchant:payment-audit', '💳', 6, 1, 0, 'ACTIVE');

-- 客户管理子菜单
(3, '客户列表', 'MENU', '/customer/list', '../views/system/UserManage.vue', 'customer:list', '📋', 1, 1, 0, 'ACTIVE'),
(3, '客户标签', 'MENU', '/customer/tags', '../views/system/UserManage.vue', 'customer:tags', '🏷️', 2, 1, 0, 'ACTIVE'),
(3, '客户统计', 'MENU', '/customer/stats', '../views/system/UserManage.vue', 'customer:stats', '📊', 3, 1, 0, 'ACTIVE'),

-- 商品管理子菜单
(4, '商品列表', 'MENU', '/product/list', '../views/onboarding/Product.vue', 'product:list', '📋', 1, 1, 0, 'ACTIVE'),
(4, '分类管理', 'MENU', '/product/category', '../views/onboarding/Product.vue', 'product:category', '📂', 2, 1, 0, 'ACTIVE'),
(4, '库存管理', 'MENU', '/product/stock', '../views/onboarding/Product.vue', 'product:stock', '📦', 3, 1, 0, 'ACTIVE'),

-- 订单管理子菜单
(5, '订单列表', 'MENU', '/order/list', '../views/onboarding/Benefit.vue', 'order:list', '📋', 1, 1, 0, 'ACTIVE'),
(5, '支付管理', 'MENU', '/order/pay', '../views/onboarding/Benefit.vue', 'order:pay', '💳', 2, 1, 0, 'ACTIVE'),
(5, '退款管理', 'MENU', '/order/refund', '../views/onboarding/Benefit.vue', 'order:refund', '💰', 3, 1, 0, 'ACTIVE'),

-- 财务管理子菜单
(6, '结算管理', 'MENU', '/finance/settlement', '../views/settlement/Index.vue', 'finance:settlement', '📊', 1, 1, 0, 'ACTIVE'),
(6, '发票管理', 'MENU', '/finance/invoice', '../views/settlement/Index.vue', 'finance:invoice', '📄', 2, 1, 0, 'ACTIVE'),
(6, '对账管理', 'MENU', '/finance/reconciliation', '../views/settlement/Index.vue', 'finance:reconciliation', '🔍', 3, 1, 0, 'ACTIVE'),

-- 风险管理子菜单
(7, '规则管理', 'MENU', '/risk/rules', '../views/audit/Index.vue', 'risk:rules', '📋', 1, 1, 0, 'ACTIVE'),
(7, '风险告警', 'MENU', '/risk/alerts', '../views/audit/Index.vue', 'risk:alerts', '🚨', 2, 1, 0, 'ACTIVE'),
(7, '交易监控', 'MENU', '/risk/monitor', '../views/audit/Index.vue', 'risk:monitor', '📈', 3, 1, 0, 'ACTIVE'),

-- 系统管理子菜单
(8, '用户管理', 'MENU', '/system/users', '../views/system/UserManage.vue', 'system:user:list', '👥', 1, 1, 0, 'ACTIVE'),
(8, '角色管理', 'MENU', '/system/roles', '../views/system/RoleManage.vue', 'system:role:list', '🎭', 2, 1, 0, 'ACTIVE'),
(8, '菜单管理', 'MENU', '/system/menus', '../views/system/MenuManage.vue', 'system:menu:list', '📑', 3, 1, 0, 'ACTIVE'),
(8, '接入平台', 'MENU', '/system/platforms', '../views/sso/Index.vue', 'system:platform:list', '🔗', 4, 1, 0, 'ACTIVE');

-- 超级管理员拥有所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu WHERE status = 'ACTIVE';

-- 系统管理员权限
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 2, id FROM sys_menu WHERE menu_name IN ('首页', '系统管理', '用户管理', '角色管理', '菜单管理', '接入平台');

-- 运营人员权限
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 3, id FROM sys_menu WHERE menu_name IN ('首页', '商户管理', '商户列表', '资质审核', '客户管理', '客户列表', '商品管理', '商品列表', '分类管理', '库存管理', '订单管理', '订单列表', '支付管理', '退款管理');

-- 审核人员权限
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 4, id FROM sys_menu WHERE menu_name IN ('首页', '商户管理', '资质审核', '风险管理', '规则管理', '风险告警', '交易监控');

-- 财务人员权限
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 5, id FROM sys_menu WHERE menu_name IN ('首页', '财务管理', '结算管理', '发票管理', '订单管理', '订单列表');

-- 为现有用户分配角色
INSERT INTO sys_user_role (user_id, role_id) SELECT id, 1 FROM sys_user WHERE username = 'admin';
INSERT INTO sys_user_role (user_id, role_id) SELECT id, 2 FROM sys_user WHERE username = 'sysadmin';
INSERT INTO sys_user_role (user_id, role_id) SELECT id, 3 FROM sys_user WHERE username = 'operator';
INSERT INTO sys_user_role (user_id, role_id) SELECT id, 4 FROM sys_user WHERE username = 'auditor';
INSERT INTO sys_user_role (user_id, role_id) SELECT id, 5 FROM sys_user WHERE username = 'finance';

COMMIT;