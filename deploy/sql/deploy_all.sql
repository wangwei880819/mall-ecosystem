-- ============================================================
-- 商城生态运营管理平台 — 一键部署/更新脚本
-- 生成时间: 2026-08-17
-- 用途: 表结构变更 + 种子数据 + 模拟数据（安全可重复执行）
-- 执行: mysql -u igou -p igou_mall < deploy_all.sql
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 第1部分: 补齐缺失表
-- ============================================================

CREATE TABLE IF NOT EXISTS banner (
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_url VARCHAR(500) DEFAULT '' COMMENT '图片URL',
    link_url VARCHAR(500) DEFAULT '' COMMENT '跳转链接',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='轮播图';

CREATE TABLE IF NOT EXISTS customer_behavior (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    behavior_type VARCHAR(32) NOT NULL COMMENT '行为类型：VIEW/PURCHASE/COLLECT/CART/SEARCH/SHARE',
    target_type VARCHAR(32) DEFAULT NULL COMMENT '目标类型：PRODUCT/CATEGORY/MERCHANT',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
    behavior_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    ip_address VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
    device_type VARCHAR(32) DEFAULT NULL COMMENT '设备类型：PC/MOBILE/TABLET',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_behavior_type (behavior_type),
    KEY idx_behavior_time (behavior_time),
    KEY idx_customer_behavior (customer_id, behavior_type, behavior_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户行为记录表';

CREATE TABLE IF NOT EXISTS customer_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    tag_name VARCHAR(64) NOT NULL COMMENT '标签名称',
    tag_category VARCHAR(64) DEFAULT NULL COMMENT '标签分类：CONSUMPTION/BEHAVIOR/INTEREST/DEMOGRAPHIC',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_tag_category (tag_category),
    KEY idx_customer_tag (customer_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户标签表';

CREATE TABLE IF NOT EXISTS delivery_address (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    province VARCHAR(64) NOT NULL COMMENT '省份',
    city VARCHAR(64) NOT NULL COMMENT '城市',
    district VARCHAR(64) NOT NULL COMMENT '区县',
    detail_address VARCHAR(512) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DELETED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';

CREATE TABLE IF NOT EXISTS home_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) DEFAULT '' COMMENT '配置值',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页配置';

CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    invoice_code VARCHAR(64) DEFAULT NULL COMMENT '发票代码',
    invoice_no VARCHAR(64) DEFAULT NULL COMMENT '发票号码',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    invoice_type VARCHAR(32) NOT NULL DEFAULT 'PERSONAL' COMMENT '发票类型：PERSONAL/COMPANY/ELECTRONIC',
    title VARCHAR(256) NOT NULL COMMENT '发票抬头',
    tax_number VARCHAR(64) DEFAULT NULL COMMENT '纳税人识别号',
    amount DECIMAL(12,2) NOT NULL COMMENT '发票金额',
    status VARCHAR(32) NOT NULL DEFAULT 'APPLIED' COMMENT '状态：APPLIED/PROCESSING/ISSUED/FAILED',
    issue_time DATETIME DEFAULT NULL COMMENT '开具时间',
    pdf_url VARCHAR(512) DEFAULT NULL COMMENT '发票PDF URL',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_code (order_code),
    KEY idx_customer_id (customer_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='发票表';

CREATE TABLE IF NOT EXISTS merchant_qualification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    qual_type VARCHAR(64) NOT NULL COMMENT '资质类型：BUSINESS_LICENSE/TAX_CERTIFICATE/LEGAL_PERSON_ID/OTHER',
    qual_name VARCHAR(128) NOT NULL COMMENT '资质名称',
    qual_file_url VARCHAR(512) NOT NULL COMMENT '资质文件URL',
    audit_status VARCHAR(32) DEFAULT 'PENDING' COMMENT '审核状态：PENDING/PASSED/REJECTED',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    auditor VARCHAR(64) DEFAULT NULL COMMENT '审核人',
    audit_comment VARCHAR(512) DEFAULT NULL COMMENT '审核意见',
    expire_date DATE DEFAULT NULL COMMENT '有效期至',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_merchant_id (merchant_id),
    KEY idx_qual_type (qual_type),
    KEY idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商户资质表';

CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category_code VARCHAR(32) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    level INT NOT NULL DEFAULT 1 COMMENT '分类级别：1/2/3',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    icon_url VARCHAR(512) DEFAULT NULL COMMENT '图标URL',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_id (parent_id),
    KEY idx_level (level),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS reconciliation_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    recon_code VARCHAR(32) NOT NULL COMMENT '对账编号',
    recon_period VARCHAR(32) NOT NULL COMMENT '对账周期',
    total_order_count INT DEFAULT 0 COMMENT '订单总数',
    total_order_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '订单总金额',
    pay_order_count INT DEFAULT 0 COMMENT '已支付订单数',
    pay_order_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '已支付金额',
    diff_count INT DEFAULT 0 COMMENT '差异订单数',
    diff_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '差异金额',
    status VARCHAR(32) NOT NULL DEFAULT 'PROCESSING' COMMENT '状态：PROCESSING/SUCCESS/FAILED',
    result_detail TEXT COMMENT '对账结果详情',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recon_code (recon_code),
    KEY idx_period (recon_period),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账记录表';

CREATE TABLE IF NOT EXISTS refund_apply (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    refund_type VARCHAR(32) NOT NULL COMMENT '退款类型：REFUND_ONLY/REFUND_WITH_RETURN',
    refund_amount DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(512) DEFAULT NULL COMMENT '退款原因',
    images VARCHAR(2048) DEFAULT NULL COMMENT '凭证图片（逗号分隔）',
    status VARCHAR(32) NOT NULL DEFAULT 'APPLIED' COMMENT '状态：APPLIED/REVIEWING/APPROVED/REJECTED/REFUNDED',
    merchant_remark VARCHAR(512) DEFAULT NULL COMMENT '商户备注',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    auditor VARCHAR(64) DEFAULT NULL COMMENT '审核人',
    refund_time DATETIME DEFAULT NULL COMMENT '退款时间',
    refund_no VARCHAR(64) DEFAULT NULL COMMENT '退款流水号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_order_code (order_code),
    KEY idx_status (status),
    KEY idx_customer_id (customer_id),
    KEY idx_merchant_id (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款申请表';

CREATE TABLE IF NOT EXISTS settlement_detail (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    settle_id BIGINT NOT NULL COMMENT '结算ID',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    order_amount DECIMAL(12,2) NOT NULL COMMENT '订单金额',
    commission_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '佣金比例',
    commission_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '佣金金额',
    ai_dou_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT 'AI豆金额',
    merchant_amount DECIMAL(12,2) NOT NULL COMMENT '商户应收金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_settle_id (settle_id),
    KEY idx_order_code (order_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结算明细表';

CREATE TABLE IF NOT EXISTS stock_change (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    change_type VARCHAR(32) NOT NULL COMMENT '变动类型：IN/OUT/ADJUST',
    change_amount INT NOT NULL COMMENT '变动数量',
    before_stock INT NOT NULL COMMENT '变动前库存',
    after_stock INT NOT NULL COMMENT '变动后库存',
    order_code VARCHAR(32) DEFAULT NULL COMMENT '关联订单编号',
    operator VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    reason VARCHAR(256) DEFAULT NULL COMMENT '变动原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_product_id (product_id),
    KEY idx_change_type (change_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存变动记录表';

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    log_type VARCHAR(32) NOT NULL COMMENT '日志类型：OPERATION/AUDIT/ERROR/ACCESS',
    operator VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operation VARCHAR(128) DEFAULT NULL COMMENT '操作描述',
    module VARCHAR(64) DEFAULT NULL COMMENT '模块名称',
    target_type VARCHAR(32) DEFAULT NULL COMMENT '目标类型',
    target_id BIGINT DEFAULT NULL COMMENT '目标ID',
    detail TEXT COMMENT '操作详情（JSON）',
    ip_address VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
    user_agent VARCHAR(512) DEFAULT NULL COMMENT '用户代理',
    result VARCHAR(16) DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_log_type (log_type),
    KEY idx_operator (operator),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统日志表';

CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    description VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

CREATE TABLE IF NOT EXISTS transaction_monitor (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    monitor_code VARCHAR(32) NOT NULL COMMENT '监控编号',
    monitor_type VARCHAR(32) NOT NULL COMMENT '监控类型：REAL_TIME/DAILY/HOURLY',
    metric_type VARCHAR(64) NOT NULL COMMENT '指标类型：ORDER_COUNT/ORDER_AMOUNT/PAY_SUCCESS_RATE/REFUND_RATE',
    threshold_value DECIMAL(12,2) DEFAULT NULL COMMENT '阈值',
    current_value DECIMAL(12,2) DEFAULT NULL COMMENT '当前值',
    is_breach TINYINT DEFAULT 0 COMMENT '是否超限：0-否 1-是',
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL/WARNING/CRITICAL',
    monitor_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '监控时间',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_monitor_code (monitor_code),
    KEY idx_monitor_type (monitor_type),
    KEY idx_metric_type (metric_type),
    KEY idx_is_breach (is_breach),
    KEY idx_monitor_time (monitor_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易监控表';

-- ============================================================
-- 第2部分: 风控稽核管理平台种子数据
-- ============================================================

INSERT INTO blacklist_item (id, value, type, list_type, reason, source, operator, create_time, expire_time) VALUES
(1, '138****1234', 'PHONE', 'BLACK', '多次恶意退款，累计退款率85%', '系统自动', '风控系统', '2026-07-20 14:30:00', '2027-07-20 14:30:00'),
(2, '192.168.1.200', 'IP', 'BLACK', '批量注册攻击源IP', '安全组', '管理员', '2026-07-18 09:15:00', NULL),
(3, 'DEV_EMU_20260701', 'DEVICE', 'BLACK', '模拟器设备指纹，关联多个欺诈账号', '系统自动', '风控系统', '2026-07-15 16:45:00', NULL),
(4, '91110108MA01XXXXX', 'CREDIT_CODE', 'BLACK', '营业执照造假，已被市场监管部门列入异常', '外部数据', '审核员', '2026-07-10 11:20:00', NULL),
(5, 'MER20260001', 'MERCHANT', 'BLACK', '多次违规经营，已被平台清退', '运营组', '管理员', '2026-07-05 08:00:00', NULL),
(6, '139****5678', 'PHONE', 'WHITE', '平台VIP大客户', '运营组', '管理员', '2026-06-01 10:00:00', NULL),
(7, '10.0.0.100', 'IP', 'WHITE', '公司内部测试IP', '技术组', '管理员', '2026-05-15 09:00:00', NULL),
(8, '137****9012', 'PHONE', 'GRAY', '行为异常监测中，退款率45%', '系统自动', '风控系统', '2026-07-25 14:00:00', '2026-08-25 14:00:00'),
(9, '13531255252', 'PHONE', 'BLACK', '测试使用', '手动添加', '管理员', '2026-07-28 18:56:32', NULL)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO risk_event (id, event_type, target, risk_level, score, hit_rule, status, source, detail, create_time, update_time) VALUES
(1, '下单风控', '用户138****8001 / 商户:数码旗舰店', 'HIGH', 92, '订单金额异常', 'BLOCKED', '生态合作平台', '用户单笔订单金额超过历史平均值500%，且收货地址为近期新增地址', '2026-07-27 10:32:15', '2026-07-28 14:07:21'),
(2, '注册风控', 'IP:192.168.1.100', 'MEDIUM', 65, '注册频率异常', 'BLOCKED', '生态合作平台', '同一IP地址30分钟内注册超过10个账号', '2026-07-27 10:28:40', '2026-07-27 10:28:40'),
(3, '支付风控', '用户139****8002 / 订单:ORD20260727003', 'HIGH', 88, '异地支付检测', 'PENDING', '生态合作平台', '用户登录地和支付IP地址跨省，且支付金额较大', '2026-07-27 10:15:22', '2026-07-27 10:15:22'),
(4, '评价风控', '商户:美妆集合店 / 商品SPU0001', 'LOW', 35, '评价内容敏感', 'PASSED', '生态合作平台', '评价内容包含敏感词，但经分析为正常评价', '2026-07-27 09:58:03', '2026-07-27 09:58:03'),
(5, '退款风控', '用户137****8003', 'MEDIUM', 72, '退款频率异常', 'PENDING', '生态合作平台', '该用户近7天退款率超过60%，疑似恶意退款', '2026-07-27 09:45:11', '2026-07-27 09:45:11'),
(6, '登录风控', '设备指纹:DEV_UNKNOWN_001', 'HIGH', 85, '设备指纹异常', 'MANUAL', '生态合作平台', '检测到设备指纹模拟器特征，疑似使用虚拟设备', '2026-07-27 09:30:55', '2026-07-27 09:30:55'),
(7, '下单风控', '商户:本地生活馆', 'MEDIUM', 58, '价格异常波动', 'PENDING', '生态合作平台', '商品价格在1小时内变动超过200%，疑似价格操纵', '2026-07-27 09:15:28', '2026-07-27 09:15:28'),
(8, '入驻风控', '商户申请:XX科技有限公司', 'HIGH', 90, '资质材料异常', 'PENDING', '生态合作平台', '营业执照图片存在PS痕迹，统一社会信用代码在黑名单中', '2026-07-27 08:50:12', '2026-07-27 08:50:12')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO risk_check_rule (id, name, type, scene, priority, rule_condition, action, hit_count, active, description, create_time, update_time) VALUES
(1, '订单金额异常检测', 'CONDITION', '下单风控', 1, 'order.amount > avgAmount * 5 AND user.accountAge < 7天', 'BLOCK', 3250, 1, '检测订单金额是否远超历史均值', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(2, '设备指纹异常检测', 'SCRIPT', '登录风控', 2, 'device.fingerprint in virtualDeviceList OR device.emulator == true', 'BLOCK', 2890, 1, '检测异常设备指纹和模拟器特征', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(3, '注册频率限制', 'FREQUENCY', '注册风控', 3, 'register.count > 10 PER 30min FROM sameIP', 'BLOCK', 2150, 1, '限制同IP高频注册行为', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(4, '退款频率异常', 'FREQUENCY', '退款风控', 4, 'refund.rate > 60% AND refund.count > 5 PER 7天', 'MANUAL', 1820, 1, '检测异常退款行为模式', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(5, '商品价格异常波动', 'CONDITION', '下单风控', 5, 'product.price.change > 200% PER 1h', 'DOWNGRADE', 1560, 1, '检测商品价格异常波动', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(6, '评价内容敏感词过滤', 'SCRIPT', '评价风控', 6, 'review.content contains sensitiveKeywords', 'MANUAL', 1280, 1, '过滤评价中的敏感内容', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(7, '商户资质材料异常', 'SCRIPT', '入驻风控', 1, 'merchant.creditCode in blacklist OR businessLicense.PS_Score > 0.8', 'BLOCK', 980, 1, '检测商户资质造假风险', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(8, '异地支付检测', 'RELATION', '支付风控', 2, 'user.loginProvince != pay.ipProvince AND order.amount > 500', 'MANUAL', 750, 1, '检测异地大额支付行为', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(9, '账号关联检测', 'RELATION', '注册风控', 3, 'accounts linked by device OR ip OR payment', 'DOWNGRADE', 520, 1, '检测多账号关联关系', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
(10, '优惠券滥用检测', 'FREQUENCY', '下单风控', 4, 'coupon.use.count > 20 PER 1天 FROM sameAccount', 'ALERT', 380, 0, '检测优惠券异常使用行为', '2026-07-01 00:00:00', '2026-07-27 10:00:00')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO disposal_config (id, name, type, trigger_rule, risk_level, duration, status, exec_count, description, create_time, update_time) VALUES
(1, '高风险订单自动拦截', 'BLOCK', '订单金额异常检测', 'HIGH', '24小时', 'ACTIVE', 3250, '对高风险订单进行自动拦截处理', '2026-07-01 00:00:00', '2026-07-25 14:00:00'),
(2, '异常设备登录拦截', 'BLOCK', '设备指纹异常检测', 'HIGH', '永久', 'ACTIVE', 2890, '拦截异常设备的登录请求', '2026-07-01 00:00:00', '2026-07-25 10:00:00'),
(3, '退款异常人工审核', 'MANUAL', '退款频率异常', 'MEDIUM', '7天', 'ACTIVE', 1820, '对异常退款行为转人工审核', '2026-07-01 00:00:00', '2026-07-24 16:30:00'),
(4, '价格操纵降权处理', 'DOWNGRADE', '商品价格异常波动', 'MEDIUM', '30天', 'ACTIVE', 1560, '对价格操纵商户进行降权处理', '2026-07-01 00:00:00', '2026-07-24 11:00:00'),
(5, '资质造假冻结账户', 'FREEZE', '商户资质材料异常', 'HIGH', '永久', 'ACTIVE', 980, '冻结资质造假商户的账户', '2026-07-01 00:00:00', '2026-07-23 09:00:00'),
(6, '异地支付安全审核', 'MANUAL', '异地支付检测', 'HIGH', '24小时', 'ACTIVE', 750, '对异地大额支付进行安全审核', '2026-07-01 00:00:00', '2026-07-22 15:00:00'),
(7, '敏感评价Webhook通知', 'WEBHOOK', '评价内容敏感词过滤', 'LOW', '1小时', 'ACTIVE', 1280, '通过Webhook通知运营处理敏感评价', '2026-07-01 00:00:00', '2026-07-21 10:00:00'),
(8, '批量注册限制登录', 'LOGIN_LIMIT', '注册频率限制', 'HIGH', '7天', 'INACTIVE', 2150, '限制批量注册账号的登录权限', '2026-07-01 00:00:00', '2026-07-20 08:00:00')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================
-- 第3部分: 新增表字段（幂等：列已存在时跳过）
-- 涵盖: merchant / product / customer / mall_order / customer_tag / commission_config
-- ============================================================
DROP PROCEDURE IF EXISTS safe_add_column;
DELIMITER $$
CREATE PROCEDURE safe_add_column(IN tName VARCHAR(128), IN cName VARCHAR(128), IN cDef VARCHAR(1024))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = tName AND column_name = cName
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', tName, ' ADD COLUMN ', cName, ' ', cDef);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 3.1 商户表 merchant（19列）
CALL safe_add_column('merchant', 'password', 'VARCHAR(128) COMMENT ''登录密码''');
CALL safe_add_column('merchant', 'province', 'VARCHAR(64) COMMENT ''省份''');
CALL safe_add_column('merchant', 'city', 'VARCHAR(64) COMMENT ''城市''');
CALL safe_add_column('merchant', 'district', 'VARCHAR(64) COMMENT ''区县''');
CALL safe_add_column('merchant', 'address', 'VARCHAR(500) COMMENT ''详细地址''');
CALL safe_add_column('merchant', 'bank_name', 'VARCHAR(128) COMMENT ''开户行''');
CALL safe_add_column('merchant', 'bank_account', 'VARCHAR(64) COMMENT ''银行账号''');
CALL safe_add_column('merchant', 'tax_number', 'VARCHAR(64) COMMENT ''税号''');
CALL safe_add_column('merchant', 'settle_cycle', 'VARCHAR(32) COMMENT ''结算周期''');
CALL safe_add_column('merchant', 'status', 'VARCHAR(32) DEFAULT ''ACTIVE'' COMMENT ''状态''');
CALL safe_add_column('merchant', 'reject_reason', 'VARCHAR(500) COMMENT ''驳回原因''');
CALL safe_add_column('merchant', 'industry', 'VARCHAR(64) COMMENT ''所属行业''');
CALL safe_add_column('merchant', 'credit_score', 'INT DEFAULT 0 COMMENT ''信用评分''');
CALL safe_add_column('merchant', 'legal_person_id', 'VARCHAR(64) COMMENT ''法人身份证号''');
CALL safe_add_column('merchant', 'trademark_no', 'VARCHAR(64) COMMENT ''商标号''');
CALL safe_add_column('merchant', 'auth_chain', 'VARCHAR(2000) COMMENT ''授权链''');
CALL safe_add_column('merchant', 'category_match', 'VARCHAR(2000) COMMENT ''类目匹配''');
CALL safe_add_column('merchant', 'audit_node', 'VARCHAR(50) COMMENT ''当前审核节点''');
CALL safe_add_column('merchant', 'audit_node_deadline', 'DATETIME COMMENT ''审核节点截止时间''');

-- 3.2 商品表 product（16列，含审核+展示字段）
CALL safe_add_column('product', 'review_level', 'INT DEFAULT NULL COMMENT ''审核级别：null=待一级审核, 1=待二级审核, 2=已通过''');
CALL safe_add_column('product', 'level1_audit_time', 'DATETIME DEFAULT NULL COMMENT ''一级审核时间''');
CALL safe_add_column('product', 'level1_auditor', 'VARCHAR(64) DEFAULT NULL COMMENT ''一级审核人''');
CALL safe_add_column('product', 'level2_audit_time', 'DATETIME DEFAULT NULL COMMENT ''二级审核时间''');
CALL safe_add_column('product', 'level2_auditor', 'VARCHAR(64) DEFAULT NULL COMMENT ''二级审核人''');
CALL safe_add_column('product', 'merchant_name', 'VARCHAR(128) DEFAULT NULL COMMENT ''商户名称''');
CALL safe_add_column('product', 'vip_price', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''VIP价格''');
CALL safe_add_column('product', 'update_time', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''');
CALL safe_add_column('product', 'reject_reason', 'VARCHAR(500) COMMENT ''驳回原因''');
CALL safe_add_column('product', 'is_hot', 'TINYINT DEFAULT 0 COMMENT ''是否热销''');
CALL safe_add_column('product', 'is_new', 'TINYINT DEFAULT 0 COMMENT ''是否新品''');
CALL safe_add_column('product', 'is_recommend', 'TINYINT DEFAULT 0 COMMENT ''是否推荐''');
CALL safe_add_column('product', 'sort_order', 'INT DEFAULT 0 COMMENT ''排序号''');
CALL safe_add_column('product', 'approve_reason', 'VARCHAR(500) COMMENT ''审核通过说明''');
CALL safe_add_column('product', 'auditor', 'VARCHAR(64) COMMENT ''审核人''');
CALL safe_add_column('product', 'audit_time', 'DATETIME COMMENT ''审核时间''');

-- 3.3 客户表 customer（6列）
CALL safe_add_column('customer', 'avatar', 'VARCHAR(512) COMMENT ''头像URL''');
CALL safe_add_column('customer', 'email', 'VARCHAR(128) COMMENT ''邮箱''');
CALL safe_add_column('customer', 'total_amount', 'DECIMAL(12,2) DEFAULT 0.00 COMMENT ''累计消费金额''');
CALL safe_add_column('customer', 'order_count', 'INT DEFAULT 0 COMMENT ''累计订单数''');
CALL safe_add_column('customer', 'birth_date', 'DATETIME COMMENT ''出生日期''');
CALL safe_add_column('customer', 'gender', 'VARCHAR(16) COMMENT ''性别''');

-- 3.4 订单表 mall_order（4列）
CALL safe_add_column('mall_order', 'product_price', 'DECIMAL(10,2) COMMENT ''商品单价''');
CALL safe_add_column('mall_order', 'refund_status', 'VARCHAR(32) COMMENT ''退款状态''');
CALL safe_add_column('mall_order', 'delivery_status', 'VARCHAR(32) COMMENT ''配送状态''');
CALL safe_add_column('mall_order', 'address_id', 'BIGINT COMMENT ''收货地址ID''');

-- 3.5 客户标签表 customer_tag（1列）
CALL safe_add_column('customer_tag', 'tag_type', 'VARCHAR(64) COMMENT ''标签类型''');

-- 3.6 佣金配置表 commission_config（3列）
CALL safe_add_column('commission_config', 'settle_type', 'VARCHAR(32) COMMENT ''结算类型：AI_DOU/COMMISSION/EXPANSION''');
CALL safe_add_column('commission_config', 'settle_period', 'VARCHAR(32) DEFAULT ''MONTHLY'' COMMENT ''结算周期：MONTHLY/WEEKLY/DAILY''');
CALL safe_add_column('commission_config', 'min_settle_amount', 'DECIMAL(12,2) DEFAULT 100.00 COMMENT ''最低结算额''');

-- 3.7 评价表 evaluation（补齐所有缺失列 + 处理 user_phone→customer_phone 重命名）
-- 3.7a 处理旧表列名：user_phone → customer_phone
SET @has_user_phone = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'evaluation' AND column_name = 'user_phone');
SET @has_customer_phone = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'evaluation' AND column_name = 'customer_phone');
SET @rename_sql = IF(@has_user_phone > 0 AND @has_customer_phone = 0, 'ALTER TABLE evaluation CHANGE COLUMN user_phone customer_phone VARCHAR(20) COMMENT ''客户手机号''', 'SELECT ''rename: skip'' AS msg');
PREPARE rename_stmt FROM @rename_sql;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

-- 3.7b 补齐缺失列
CALL safe_add_column('evaluation', 'customer_id', 'BIGINT DEFAULT NULL COMMENT ''客户ID''');
CALL safe_add_column('evaluation', 'customer_phone', 'VARCHAR(20) COMMENT ''客户手机号''');
CALL safe_add_column('evaluation', 'score_quality', 'TINYINT NOT NULL DEFAULT 5 COMMENT ''商品质量评分''');
CALL safe_add_column('evaluation', 'score_delivery', 'TINYINT NOT NULL DEFAULT 5 COMMENT ''配送服务评分''');
CALL safe_add_column('evaluation', 'score_service', 'TINYINT NOT NULL DEFAULT 5 COMMENT ''服务态度评分''');
CALL safe_add_column('evaluation', 'score_aftersale', 'TINYINT NOT NULL DEFAULT 5 COMMENT ''售后服务评分''');
CALL safe_add_column('evaluation', 'score_value', 'TINYINT NOT NULL DEFAULT 5 COMMENT ''性价比评分''');
CALL safe_add_column('evaluation', 'content', 'VARCHAR(2000) COMMENT ''评价内容''');
CALL safe_add_column('evaluation', 'tags', 'VARCHAR(256) COMMENT ''评价标签''');
CALL safe_add_column('evaluation', 'sentiment', 'VARCHAR(16) DEFAULT ''POSITIVE'' COMMENT ''情感倾向''');
CALL safe_add_column('evaluation', 'ai_status', 'VARCHAR(32) DEFAULT ''AUTO_PASS'' COMMENT ''AI审核状态''');
CALL safe_add_column('evaluation', 'merchant_reply', 'VARCHAR(2000) COMMENT ''商家回复''');
CALL safe_add_column('evaluation', 'reply_time', 'DATETIME COMMENT ''回复时间''');

-- 3.8 系统日志表 sys_log（补齐 LogAspect 写入的字段）
CALL safe_add_column('sys_log', 'request_uri', 'VARCHAR(512) COMMENT ''请求URI''');
CALL safe_add_column('sys_log', 'request_method', 'VARCHAR(16) COMMENT ''请求方法''');
CALL safe_add_column('sys_log', 'request_params', 'TEXT COMMENT ''请求参数''');
CALL safe_add_column('sys_log', 'response_body', 'TEXT COMMENT ''响应内容''');
CALL safe_add_column('sys_log', 'cost_time', 'BIGINT COMMENT ''耗时(ms)''');

DROP PROCEDURE IF EXISTS safe_add_column;

-- ============================================================
-- 第4部分: 补充系统管理菜单 - 日志管理
-- ============================================================
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, sort_order, visible, keep_alive, status) 
SELECT 48, 8, '日志管理', 'MENU', '/system/logs', 'system/LogManage', '📋', 5, 1, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 48);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, 48 FROM sys_role r WHERE r.role_code = 'SUPER_ADMIN'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = 48);

-- ============================================================
-- 第5部分: 商品分类初始化数据
-- ============================================================
INSERT INTO product_category (id, category_code, category_name, parent_id, level, sort_order, icon_url, status, create_time, update_time) VALUES
(1,  'CAT001',   '视频娱乐', 0, 1, 1, NULL, 'ACTIVE', NOW(), NOW()),
(2,  'CAT002',   '餐饮美食', 0, 1, 2, NULL, 'ACTIVE', NOW(), NOW()),
(3,  'CAT003',   '生活服务', 0, 1, 3, NULL, 'ACTIVE', NOW(), NOW()),
(4,  'CAT004',   '数字商品', 0, 1, 4, NULL, 'ACTIVE', NOW(), NOW()),
(5,  'CAT005',   '旅游出行', 0, 1, 5, NULL, 'ACTIVE', NOW(), NOW()),
(6,  'CAT006',   '购物权益', 0, 1, 6, NULL, 'ACTIVE', NOW(), NOW()),
(17, 'CAT007',   '权益商品', 0, 1, 7, NULL, 'ACTIVE', NOW(), NOW()),
(7,  'CAT00101', '视频会员', 1, 2, 1, NULL, 'ACTIVE', NOW(), NOW()),
(8,  'CAT00102', '音乐会员', 1, 2, 2, NULL, 'ACTIVE', NOW(), NOW()),
(9,  'CAT00201', '咖啡饮品', 2, 2, 1, NULL, 'ACTIVE', NOW(), NOW()),
(10, 'CAT00202', '餐饮套餐', 2, 2, 2, NULL, 'ACTIVE', NOW(), NOW()),
(11, 'CAT00301', '家政服务', 3, 2, 1, NULL, 'ACTIVE', NOW(), NOW()),
(12, 'CAT00302', '美容美发', 3, 2, 2, NULL, 'ACTIVE', NOW(), NOW()),
(13, 'CAT00401', '游戏点卡', 4, 2, 1, NULL, 'ACTIVE', NOW(), NOW()),
(14, 'CAT00402', '软件激活', 4, 2, 2, NULL, 'ACTIVE', NOW(), NOW()),
(15, 'CAT00501', '酒店预订', 5, 2, 1, NULL, 'ACTIVE', NOW(), NOW()),
(16, 'CAT00502', '机票预订', 5, 2, 2, NULL, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  category_name = VALUES(category_name),
  parent_id     = VALUES(parent_id),
  level         = VALUES(level),
  sort_order    = VALUES(sort_order),
  status        = VALUES(status),
  update_time   = NOW();

-- ============================================================
-- 第6部分: 模拟商品数据（供一级/二级选品审核）
-- ============================================================
INSERT INTO product (product_code, merchant_id, product_name, product_type, category_id, category, price, market_price, stock, image_urls, status, review_level, create_time) VALUES
('PRD2408001', 1, '腾讯视频VIP会员月卡', 'DIGITAL', 1, '视频会员', 19.90, 25.00, 999, '/uploads/products/img1.png', 'PENDING', NULL, NOW()),
('PRD2408002', 1, '爱奇艺黄金会员月卡', 'DIGITAL', 1, '视频会员', 22.90, 30.00, 888, '/uploads/products/img2.png', 'PENDING', NULL, NOW()),
('PRD2408003', 2, '网易云音乐黑胶VIP季卡', 'DIGITAL', 2, '音乐会员', 45.00, 58.00, 500, '/uploads/products/img3.png', 'PENDING', NULL, NOW()),
('PRD2408004', 2, 'QQ音乐绿钻豪华版年卡', 'DIGITAL', 2, '音乐会员', 168.00, 198.00, 300, '/uploads/products/img4.png', 'PENDING', NULL, NOW()),
('PRD2408005', 3, '百度网盘超级会员月卡', 'DIGITAL', 3, '云存储', 25.00, 30.00, 600, '/uploads/products/img5.png', 'PENDING', NULL, NOW()),
('PRD2408006', 3, 'WPS会员年卡', 'DIGITAL', 4, '办公软件', 89.00, 118.00, 400, '/uploads/products/img6.png', 'PENDING', NULL, NOW()),
('PRD2408007', 1, '哔哩哔哩大会员年卡', 'DIGITAL', 1, '视频会员', 148.00, 168.00, 200, '/uploads/products/img7.png', 'PENDING', NULL, NOW()),
('PRD2408008', 2, '饿了么超级会员年卡', 'DIGITAL', 5, '生活服务', 108.00, 128.00, 350, '/uploads/products/img8.png', 'PENDING', NULL, NOW())
ON DUPLICATE KEY UPDATE product_code=product_code;

INSERT INTO product (product_code, merchant_id, product_name, product_type, category_id, category, price, market_price, stock, image_urls, status, review_level, level1_audit_time, level1_auditor, create_time) VALUES
('PRD2408009', 1, '京东PLUS会员年卡', 'DIGITAL', 5, '生活服务', 99.00, 128.00, 500, '/uploads/products/img9.png', 'ONE_PASSED', 1, '2024-08-15 10:30:00', '审核员', '2024-08-14 09:00:00'),
('PRD2408010', 2, '美团外卖会员月卡', 'DIGITAL', 5, '生活服务', 15.00, 20.00, 800, '/uploads/products/img10.png', 'ONE_PASSED', 1, '2024-08-15 14:00:00', '审核员', '2024-08-14 09:00:00'),
('PRD2408011', 3, 'Keep会员季卡', 'DIGITAL', 6, '运动健康', 68.00, 88.00, 250, '/uploads/products/img11.png', 'ONE_PASSED', 1, '2024-08-16 09:00:00', '审核员', '2024-08-15 09:00:00')
ON DUPLICATE KEY UPDATE product_code=product_code;

-- ============================================================
-- 第7部分: 模拟客户数据（订单所需）
-- ============================================================
INSERT INTO customer (id, phone, password, nickname, vip_level, status, register_time, create_time) VALUES
(9,  '13800138009', '$2a$10$placeholder', '测试用户9',  'NORMAL', 'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(10, '13800138010', '$2a$10$placeholder', '测试用户10', 'NORMAL', 'ACTIVE', '2026-01-01 00:00:00', '2026-01-01 00:00:00')
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================
-- 第8部分: 模拟订单数据（100条）
-- ============================================================
INSERT INTO mall_order (order_code, customer_id, customer_phone, merchant_id, product_id, product_name, product_image, price, quantity, order_amount, pay_amount, discount_amount, ai_dou_deduct, status, pay_method, pay_no, logistics_company, logistics_no, fulfill_time, pay_time, refund_amount, refund_time, refund_reason, cancel_reason, cancel_time, remark, create_time) VALUES
('ORD178690000001', 1, '13800138001', 1, 1, '腾讯视频VIP会员月卡', '/uploads/products/img1.png', 19.90, 1, 19.90, 19.90, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260801001', '顺丰速运', 'SF1234567890', '2026-08-02 10:30:00', '2026-08-01 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-01 09:30:00'),
('ORD178690000002', 2, '13800138002', 1, 2, '爱奇艺黄金会员月卡', '/uploads/products/img2.png', 22.00, 1, 22.00, 22.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260801002', '京东物流', 'JD9876543210', '2026-08-02 14:00:00', '2026-08-01 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-01 08:45:00'),
('ORD178690000003', 3, '13800138003', 2, 3, '瑞幸咖啡29元通兑券', '/uploads/products/img3.png', 19.90, 2, 39.80, 39.80, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260801003', '中通快递', 'ZT1122334455', '2026-08-03 16:00:00', '2026-08-01 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-01 13:30:00'),
('ORD178690000004', 4, '13800138004', 2, 4, 'QQ音乐绿钻豪华版月卡', '/uploads/products/img4.png', 15.00, 3, 45.00, 45.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260801004', '圆通快递', 'YT5566778899', '2026-08-02 11:00:00', '2026-08-01 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-01 10:45:00'),
('ORD178690000005', 5, '13800138005', 3, 5, '美团外卖20元红包', '/uploads/products/img5.png', 18.00, 2, 36.00, 36.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260801005', '美团配送', 'MT3344556677', '2026-08-02 18:00:00', '2026-08-01 16:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-01 15:30:00'),
('ORD178690000006', 6, '13800138006', 3, 6, '王者荣耀60点券', '/uploads/products/img6.png', 6.00, 1, 6.00, 6.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260802001', NULL, NULL, NULL, '2026-08-02 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-02 08:50:00'),
('ORD178690000007', 7, '13800138007', 1, 7, '网易云音乐黑胶VIP月卡', '/uploads/products/img7.png', 18.00, 1, 18.00, 18.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260802002', '顺丰速运', 'SF2233445566', '2026-08-03 09:00:00', '2026-08-02 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-02 09:45:00'),
('ORD178690000008', 8, '13800138008', 2, 8, '星巴克星礼卡100元', '/uploads/products/img8.png', 95.00, 1, 95.00, 90.00, 5.00, 5.00, 'EVALUATED', 'WECHAT', 'PAY20260802003', '京东物流', 'JD7788990011', '2026-08-03 14:00:00', '2026-08-02 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-02 13:30:00'),
('ORD178690000009', 1, '13800138001', 3, 9, '麦当劳麦辣鸡腿堡套餐', '/uploads/products/img9.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260802004', '美团配送', 'MT8899001122', '2026-08-03 12:00:00', '2026-08-02 12:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-02 11:30:00'),
('ORD178690000010', 9, '13800138009', 1, 10, '哔哩哔哩大会员月卡', '/uploads/products/img10.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260802005', NULL, NULL, NULL, '2026-08-02 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-02 14:45:00'),
('ORD178690000011', 10, '13800138010', 2, 11, '滴滴快车10元券', '/uploads/products/img11.png', 8.00, 2, 16.00, 16.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260802006', NULL, NULL, NULL, '2026-08-02 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-02 08:45:00'),
('ORD178690000012', 2, '13800138002', 3, 12, '京东E卡100元', '/uploads/products/img12.png', 95.00, 1, 95.00, 95.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260803001', '顺丰速运', 'SF3344556677', '2026-08-04 09:00:00', '2026-08-03 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-03 09:50:00'),
('ORD178690000013', 3, '13800138003', 1, 13, '优酷VIP会员月卡', '/uploads/products/img13.png', 15.00, 1, 15.00, 15.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260803002', '圆通快递', 'YT8899001122', '2026-08-04 11:00:00', '2026-08-03 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-03 10:45:00'),
('ORD178690000014', 4, '13800138004', 2, 14, '滴滴专车30元券', '/uploads/products/img14.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260803003', NULL, NULL, NULL, '2026-08-03 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-03 13:45:00'),
('ORD178690000015', 5, '13800138005', 3, 15, '肯德基翅桶套餐', '/uploads/products/img15.png', 45.00, 1, 45.00, 45.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260803004', '美团配送', 'MT9900112233', '2026-08-03 18:00:00', '2026-08-03 17:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-03 16:45:00'),
('ORD178690000016', 6, '13800138006', 1, 1, '腾讯视频VIP会员月卡', '/uploads/products/img1.png', 19.90, 1, 19.90, 19.90, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260804001', '顺丰速运', 'SF4455667788', '2026-08-05 09:00:00', '2026-08-04 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-04 08:45:00'),
('ORD178690000017', 7, '13800138007', 2, 2, '爱奇艺黄金会员月卡', '/uploads/products/img2.png', 22.00, 1, 22.00, 22.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260804002', '京东物流', 'JD9900112233', '2026-08-05 14:00:00', '2026-08-04 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-04 13:45:00'),
('ORD178690000018', 8, '13800138008', 3, 3, '瑞幸咖啡29元通兑券', '/uploads/products/img3.png', 19.90, 3, 59.70, 59.70, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260804003', '中通快递', 'ZT4455667788', '2026-08-05 16:00:00', '2026-08-04 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-04 09:45:00'),
('ORD178690000019', 1, '13800138001', 2, 4, 'QQ音乐绿钻豪华版月卡', '/uploads/products/img4.png', 15.00, 2, 30.00, 30.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260804004', '圆通快递', 'YT9900112233', '2026-08-05 11:00:00', '2026-08-04 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-04 10:45:00'),
('ORD178690000020', 9, '13800138009', 3, 5, '美团外卖20元红包', '/uploads/products/img5.png', 18.00, 1, 18.00, 18.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260804005', '美团配送', 'MT0011223344', '2026-08-04 18:00:00', '2026-08-04 12:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-04 11:45:00'),
('ORD178690000021', 10, '13800138010', 1, 6, '王者荣耀60点券', '/uploads/products/img6.png', 6.00, 1, 6.00, 6.00, 0, 0, 'FULFILLED', 'WECHAT', 'PAY20260805001', NULL, NULL, NULL, '2026-08-05 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 08:50:00'),
('ORD178690000022', 2, '13800138002', 2, 7, '网易云音乐黑胶VIP月卡', '/uploads/products/img7.png', 18.00, 1, 18.00, 18.00, 0, 0, 'FULFILLED', 'ALIPAY', 'PAY20260805002', '顺丰速运', 'SF5566778899', '2026-08-06 09:00:00', '2026-08-05 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 09:45:00'),
('ORD178690000023', 3, '13800138003', 3, 8, '星巴克星礼卡100元', '/uploads/products/img8.png', 95.00, 1, 95.00, 90.00, 5.00, 5.00, 'FULFILLED', 'WECHAT', 'PAY20260805003', '京东物流', 'JD0011223344', '2026-08-06 14:00:00', '2026-08-05 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 13:30:00'),
('ORD178690000024', 4, '13800138004', 1, 9, '麦当劳麦辣鸡腿堡套餐', '/uploads/products/img9.png', 25.00, 1, 25.00, 25.00, 0, 0, 'FULFILLED', 'ALIPAY', 'PAY20260805004', '美团配送', 'MT1122334455', '2026-08-05 18:00:00', '2026-08-05 12:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 11:30:00'),
('ORD178690000025', 5, '13800138005', 2, 10, '哔哩哔哩大会员月卡', '/uploads/products/img10.png', 25.00, 1, 25.00, 25.00, 0, 0, 'FULFILLED', 'WECHAT', 'PAY20260805005', NULL, NULL, NULL, '2026-08-05 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 14:45:00'),
('ORD178690000026', 6, '13800138006', 3, 11, '滴滴快车10元券', '/uploads/products/img11.png', 8.00, 3, 24.00, 24.00, 0, 0, 'FULFILLED', 'ALIPAY', 'PAY20260805006', NULL, NULL, NULL, '2026-08-05 16:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 15:45:00'),
('ORD178690000027', 7, '13800138007', 1, 12, '京东E卡100元', '/uploads/products/img12.png', 95.00, 1, 95.00, 95.00, 0, 0, 'FULFILLED', 'WECHAT', 'PAY20260805007', '顺丰速运', 'SF6677889900', '2026-08-06 09:00:00', '2026-08-05 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 09:50:00'),
('ORD178690000028', 8, '13800138008', 2, 13, '优酷VIP会员月卡', '/uploads/products/img13.png', 15.00, 1, 15.00, 15.00, 0, 0, 'FULFILLED', 'ALIPAY', 'PAY20260805008', '圆通快递', 'YT0011223344', '2026-08-06 11:00:00', '2026-08-05 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 10:45:00'),
('ORD178690000029', 1, '13800138001', 3, 14, '滴滴专车30元券', '/uploads/products/img14.png', 25.00, 1, 25.00, 25.00, 0, 0, 'FULFILLED', 'WECHAT', 'PAY20260805009', NULL, NULL, NULL, '2026-08-05 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 13:45:00'),
('ORD178690000030', 9, '13800138009', 1, 15, '肯德基翅桶套餐', '/uploads/products/img15.png', 45.00, 1, 45.00, 45.00, 0, 0, 'FULFILLED', 'ALIPAY', 'PAY20260805010', '美团配送', 'MT2233445566', '2026-08-05 18:00:00', '2026-08-05 17:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-05 16:45:00'),
('ORD178690000031', 10, '13800138010', 2, 1, '腾讯视频VIP会员月卡', '/uploads/products/img1.png', 19.90, 1, 19.90, 19.90, 0, 0, 'PAID', 'WECHAT', 'PAY20260810001', NULL, NULL, NULL, '2026-08-10 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 08:45:00'),
('ORD178690000032', 2, '13800138002', 3, 2, '爱奇艺黄金会员月卡', '/uploads/products/img2.png', 22.00, 1, 22.00, 22.00, 0, 0, 'PAID', 'ALIPAY', 'PAY20260810002', NULL, NULL, NULL, '2026-08-10 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 09:45:00'),
('ORD178690000033', 3, '13800138003', 1, 3, '瑞幸咖啡29元通兑券', '/uploads/products/img3.png', 19.90, 1, 19.90, 19.90, 0, 0, 'PAID', 'WECHAT', 'PAY20260810003', NULL, NULL, NULL, '2026-08-10 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 10:45:00'),
('ORD178690000034', 4, '13800138004', 2, 4, 'QQ音乐绿钻豪华版月卡', '/uploads/products/img4.png', 15.00, 1, 15.00, 15.00, 0, 0, 'PAID', 'ALIPAY', 'PAY20260810004', NULL, NULL, NULL, '2026-08-10 12:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 11:45:00'),
('ORD178690000035', 5, '13800138005', 3, 5, '美团外卖20元红包', '/uploads/products/img5.png', 18.00, 1, 18.00, 18.00, 0, 0, 'PAID', 'WECHAT', 'PAY20260810005', NULL, NULL, NULL, '2026-08-10 13:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 12:45:00'),
('ORD178690000036', 6, '13800138006', 1, 6, '王者荣耀60点券', '/uploads/products/img6.png', 6.00, 1, 6.00, 6.00, 0, 0, 'PAID', 'WECHAT', 'PAY20260810006', NULL, NULL, NULL, '2026-08-10 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 13:50:00'),
('ORD178690000037', 7, '13800138007', 2, 7, '网易云音乐黑胶VIP月卡', '/uploads/products/img7.png', 18.00, 1, 18.00, 18.00, 0, 0, 'PAID', 'ALIPAY', 'PAY20260810007', NULL, NULL, NULL, '2026-08-10 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 14:45:00'),
('ORD178690000038', 8, '13800138008', 3, 8, '星巴克星礼卡100元', '/uploads/products/img8.png', 95.00, 1, 95.00, 90.00, 5.00, 5.00, 'PAID', 'WECHAT', 'PAY20260810008', NULL, NULL, NULL, '2026-08-10 16:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 15:30:00'),
('ORD178690000039', 1, '13800138001', 1, 9, '麦当劳麦辣鸡腿堡套餐', '/uploads/products/img9.png', 25.00, 1, 25.00, 25.00, 0, 0, 'PAID', 'ALIPAY', 'PAY20260810009', NULL, NULL, NULL, '2026-08-10 17:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 16:30:00'),
('ORD178690000040', 9, '13800138009', 2, 10, '哔哩哔哩大会员月卡', '/uploads/products/img10.png', 25.00, 1, 25.00, 25.00, 0, 0, 'PAID', 'WECHAT', 'PAY20260810010', NULL, NULL, NULL, '2026-08-10 18:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-10 17:45:00'),
('ORD178690000041', 10, '13800138010', 3, 11, '滴滴快车10元券', '/uploads/products/img11.png', 8.00, 2, 16.00, 16.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260811001', NULL, NULL, NULL, '2026-08-11 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-11 08:45:00'),
('ORD178690000042', 2, '13800138002', 1, 12, '京东E卡100元', '/uploads/products/img12.png', 95.00, 1, 95.00, 95.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260811002', '顺丰速运', 'SF7788990011', '2026-08-12 09:00:00', '2026-08-11 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-11 09:50:00'),
('ORD178690000043', 3, '13800138003', 2, 13, '优酷VIP会员月卡', '/uploads/products/img13.png', 15.00, 1, 15.00, 15.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260811003', '圆通快递', 'YT1122334455', '2026-08-12 11:00:00', '2026-08-11 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-11 10:45:00'),
('ORD178690000044', 4, '13800138004', 3, 14, '滴滴专车30元券', '/uploads/products/img14.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260811004', NULL, NULL, NULL, '2026-08-11 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-11 13:45:00'),
('ORD178690000045', 5, '13800138005', 1, 15, '肯德基翅桶套餐', '/uploads/products/img15.png', 45.00, 1, 45.00, 45.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260811005', '美团配送', 'MT3344556677', '2026-08-11 18:00:00', '2026-08-11 17:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-11 16:45:00'),
('ORD178690000046', 6, '13800138006', 2, 1, '腾讯视频VIP会员月卡', '/uploads/products/img1.png', 19.90, 1, 19.90, 19.90, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260812001', NULL, NULL, NULL, '2026-08-12 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 08:45:00'),
('ORD178690000047', 7, '13800138007', 3, 2, '爱奇艺黄金会员月卡', '/uploads/products/img2.png', 22.00, 1, 22.00, 22.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260812002', '京东物流', 'JD1122334455', '2026-08-13 14:00:00', '2026-08-12 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 09:45:00'),
('ORD178690000048', 8, '13800138008', 1, 3, '瑞幸咖啡29元通兑券', '/uploads/products/img3.png', 19.90, 1, 19.90, 19.90, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260812003', '中通快递', 'ZT5566778899', '2026-08-13 16:00:00', '2026-08-12 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 10:45:00'),
('ORD178690000049', 1, '13800138001', 2, 4, 'QQ音乐绿钻豪华版月卡', '/uploads/products/img4.png', 15.00, 1, 15.00, 15.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260812004', '圆通快递', 'YT2233445566', '2026-08-13 11:00:00', '2026-08-12 12:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 11:45:00'),
('ORD178690000050', 9, '13800138009', 3, 5, '美团外卖20元红包', '/uploads/products/img5.png', 18.00, 1, 18.00, 18.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260812005', '美团配送', 'MT4455667788', '2026-08-12 18:00:00', '2026-08-12 13:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 12:45:00'),
('ORD178690000051', 10, '13800138010', 1, 6, '王者荣耀60点券', '/uploads/products/img6.png', 6.00, 1, 6.00, 6.00, 0, 0, 'SHIPPED', 'WECHAT', 'PAY20260812006', NULL, NULL, NULL, '2026-08-12 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 13:50:00'),
('ORD178690000052', 2, '13800138002', 2, 7, '网易云音乐黑胶VIP月卡', '/uploads/products/img7.png', 18.00, 1, 18.00, 18.00, 0, 0, 'SHIPPED', 'ALIPAY', 'PAY20260812007', '顺丰速运', 'SF8899001122', '2026-08-13 09:00:00', '2026-08-12 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 14:45:00'),
('ORD178690000053', 3, '13800138003', 3, 8, '星巴克星礼卡100元', '/uploads/products/img8.png', 95.00, 1, 95.00, 90.00, 5.00, 5.00, 'SHIPPED', 'WECHAT', 'PAY20260812008', '京东物流', 'JD2233445566', '2026-08-13 14:00:00', '2026-08-12 16:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 15:30:00'),
('ORD178690000054', 4, '13800138004', 1, 9, '麦当劳麦辣鸡腿堡套餐', '/uploads/products/img9.png', 25.00, 1, 25.00, 25.00, 0, 0, 'SHIPPED', 'ALIPAY', 'PAY20260812009', '美团配送', 'MT5566778899', '2026-08-12 18:00:00', '2026-08-12 17:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 16:30:00'),
('ORD178690000055', 5, '13800138005', 2, 10, '哔哩哔哩大会员月卡', '/uploads/products/img10.png', 25.00, 1, 25.00, 25.00, 0, 0, 'SHIPPED', 'WECHAT', 'PAY20260812010', NULL, NULL, NULL, '2026-08-12 18:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-12 17:45:00'),
('ORD178690000056', 6, '13800138006', 3, 11, '滴滴快车10元券', '/uploads/products/img11.png', 8.00, 1, 8.00, 8.00, 0, 0, 'CREATED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 09:00:00'),
('ORD178690000057', 7, '13800138007', 1, 12, '京东E卡100元', '/uploads/products/img12.png', 95.00, 1, 95.00, 95.00, 0, 0, 'CREATED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 10:00:00'),
('ORD178690000058', 8, '13800138008', 2, 13, '优酷VIP会员月卡', '/uploads/products/img13.png', 15.00, 1, 15.00, 15.00, 0, 0, 'CREATED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 11:00:00'),
('ORD178690000059', 1, '13800138001', 3, 14, '滴滴专车30元券', '/uploads/products/img14.png', 25.00, 1, 25.00, 25.00, 0, 0, 'CREATED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 12:00:00'),
('ORD178690000060', 9, '13800138009', 1, 15, '肯德基翅桶套餐', '/uploads/products/img15.png', 45.00, 1, 45.00, 45.00, 0, 0, 'CREATED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 13:00:00'),
('ORD178690000061', 10, '13800138010', 2, 1, '腾讯视频VIP会员月卡', '/uploads/products/img1.png', 19.90, 1, 19.90, 19.90, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260813001', NULL, NULL, NULL, '2026-08-13 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 13:45:00'),
('ORD178690000062', 2, '13800138002', 3, 2, '爱奇艺黄金会员月卡', '/uploads/products/img2.png', 22.00, 1, 22.00, 22.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260813002', NULL, NULL, NULL, '2026-08-13 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 14:45:00'),
('ORD178690000063', 3, '13800138003', 1, 3, '瑞幸咖啡29元通兑券', '/uploads/products/img3.png', 19.90, 1, 19.90, 19.90, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260813003', NULL, NULL, NULL, '2026-08-13 16:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 15:45:00'),
('ORD178690000064', 4, '13800138004', 2, 4, 'QQ音乐绿钻豪华版月卡', '/uploads/products/img4.png', 15.00, 1, 15.00, 15.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260813004', NULL, NULL, NULL, '2026-08-13 17:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 16:45:00'),
('ORD178690000065', 5, '13800138005', 3, 5, '美团外卖20元红包', '/uploads/products/img5.png', 18.00, 1, 18.00, 18.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260813005', NULL, NULL, NULL, '2026-08-13 18:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-13 17:45:00'),
('ORD178690000066', 6, '13800138006', 1, 6, '王者荣耀60点券', '/uploads/products/img6.png', 6.00, 1, 6.00, 6.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260814001', NULL, NULL, NULL, '2026-08-14 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 08:50:00'),
('ORD178690000067', 7, '13800138007', 2, 7, '网易云音乐黑胶VIP月卡', '/uploads/products/img7.png', 18.00, 1, 18.00, 18.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260814002', NULL, NULL, NULL, '2026-08-14 10:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 09:45:00'),
('ORD178690000068', 8, '13800138008', 3, 8, '星巴克星礼卡100元', '/uploads/products/img8.png', 95.00, 1, 95.00, 90.00, 5.00, 5.00, 'EVALUATED', 'WECHAT', 'PAY20260814003', NULL, NULL, NULL, '2026-08-14 11:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 10:30:00'),
('ORD178690000069', 1, '13800138001', 1, 9, '麦当劳麦辣鸡腿堡套餐', '/uploads/products/img9.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260814004', NULL, NULL, NULL, '2026-08-14 12:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 11:30:00'),
('ORD178690000070', 9, '13800138009', 2, 10, '哔哩哔哩大会员月卡', '/uploads/products/img10.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260814005', NULL, NULL, NULL, '2026-08-14 13:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 12:45:00'),
('ORD178690000071', 10, '13800138010', 3, 11, '滴滴快车10元券', '/uploads/products/img11.png', 8.00, 1, 8.00, 8.00, 0, 0, 'CANCELLED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '不想要了', '2026-08-13 14:00:00', NULL, '2026-08-13 13:45:00'),
('ORD178690000072', 2, '13800138002', 1, 12, '京东E卡100元', '/uploads/products/img12.png', 95.00, 1, 95.00, 95.00, 0, 0, 'CANCELLED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '地址填写错误', '2026-08-13 10:00:00', NULL, '2026-08-13 09:45:00'),
('ORD178690000073', 3, '13800138003', 2, 13, '优酷VIP会员月卡', '/uploads/products/img13.png', 15.00, 1, 15.00, 15.00, 0, 0, 'CANCELLED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '重复下单', '2026-08-13 11:00:00', NULL, '2026-08-13 10:45:00'),
('ORD178690000074', 4, '13800138004', 3, 14, '滴滴专车30元券', '/uploads/products/img14.png', 25.00, 1, 25.00, 25.00, 0, 0, 'CANCELLED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '价格不合适', '2026-08-14 12:00:00', NULL, '2026-08-14 11:45:00'),
('ORD178690000075', 5, '13800138005', 1, 15, '肯德基翅桶套餐', '/uploads/products/img15.png', 45.00, 1, 45.00, 45.00, 0, 0, 'CANCELLED', NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '选错商品', '2026-08-14 13:00:00', NULL, '2026-08-14 12:45:00'),
('ORD178690000076', 6, '13800138006', 2, 1, '腾讯视频VIP会员月卡', '/uploads/products/img1.png', 19.90, 1, 19.90, 19.90, 0, 0, 'REFUNDED', 'WECHAT', 'PAY20260814007', NULL, NULL, NULL, '2026-08-14 09:00:00', 19.90, '2026-08-14 14:00:00', '视频卡顿，体验不好', NULL, NULL, NULL, '2026-08-14 08:45:00'),
('ORD178690000077', 7, '13800138007', 3, 2, '爱奇艺黄金会员月卡', '/uploads/products/img2.png', 22.00, 1, 22.00, 22.00, 0, 0, 'REFUNDED', 'ALIPAY', 'PAY20260814008', NULL, NULL, NULL, '2026-08-14 10:00:00', 22.00, '2026-08-14 15:00:00', '片源太少，没有想看的', NULL, NULL, NULL, '2026-08-14 09:45:00'),
('ORD178690000078', 8, '13800138008', 1, 3, '瑞幸咖啡29元通兑券', '/uploads/products/img3.png', 19.90, 1, 19.90, 19.90, 0, 0, 'REFUNDED', 'WECHAT', 'PAY20260814009', NULL, NULL, NULL, '2026-08-14 11:00:00', 19.90, '2026-08-14 16:00:00', '附近门店不支持', NULL, NULL, NULL, '2026-08-14 10:45:00'),
('ORD178690000079', 1, '13800138001', 2, 4, 'QQ音乐绿钻豪华版月卡', '/uploads/products/img4.png', 15.00, 1, 15.00, 15.00, 0, 0, 'REFUNDED', 'ALIPAY', 'PAY20260814010', NULL, NULL, NULL, '2026-08-14 12:00:00', 15.00, '2026-08-14 17:00:00', '误操作购买', NULL, NULL, NULL, '2026-08-14 11:45:00'),
('ORD178690000080', 9, '13800138009', 3, 5, '美团外卖20元红包', '/uploads/products/img5.png', 18.00, 1, 18.00, 18.00, 0, 0, 'REFUNDED', 'WECHAT', 'PAY20260814011', NULL, NULL, NULL, '2026-08-14 13:00:00', 18.00, '2026-08-14 18:00:00', '配送时间太长', NULL, NULL, NULL, '2026-08-14 12:45:00'),
('ORD178690000081', 10, '13800138010', 1, 6, '王者荣耀60点券', '/uploads/products/img6.png', 6.00, 1, 6.00, 6.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260814012', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:50:00'),
('ORD178690000082', 2, '13800138002', 2, 7, '网易云音乐黑胶VIP月卡', '/uploads/products/img7.png', 18.00, 1, 18.00, 18.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260814013', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:45:00'),
('ORD178690000083', 3, '13800138003', 3, 8, '星巴克星礼卡100元', '/uploads/products/img8.png', 95.00, 1, 95.00, 90.00, 5.00, 5.00, 'EVALUATED', 'WECHAT', 'PAY20260814014', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:30:00'),
('ORD178690000084', 4, '13800138004', 1, 9, '麦当劳麦辣鸡腿堡套餐', '/uploads/products/img9.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260814015', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:30:00'),
('ORD178690000085', 5, '13800138005', 2, 10, '哔哩哔哩大会员月卡', '/uploads/products/img10.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260814016', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:45:00'),
('ORD178690000086', 6, '13800138006', 3, 11, '滴滴快车10元券', '/uploads/products/img11.png', 8.00, 1, 8.00, 8.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260814017', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:45:00'),
('ORD178690000087', 7, '13800138007', 1, 12, '京东E卡100元', '/uploads/products/img12.png', 95.00, 1, 95.00, 95.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260814018', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:50:00'),
('ORD178690000088', 8, '13800138008', 2, 13, '优酷VIP会员月卡', '/uploads/products/img13.png', 15.00, 1, 15.00, 15.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260814019', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:45:00'),
('ORD178690000089', 1, '13800138001', 3, 14, '滴滴专车30元券', '/uploads/products/img14.png', 25.00, 1, 25.00, 25.00, 0, 0, 'EVALUATED', 'ALIPAY', 'PAY20260814020', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:45:00'),
('ORD178690000090', 9, '13800138009', 1, 15, '肯德基翅桶套餐', '/uploads/products/img15.png', 45.00, 1, 45.00, 45.00, 0, 0, 'EVALUATED', 'WECHAT', 'PAY20260814021', NULL, NULL, NULL, '2026-08-14 14:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 13:45:00'),
('ORD178690000091', 1, '13800138001', 1, 1, '腾讯视频VIP会员月卡', '/uploads/products/img1.png', 19.90, 1, 19.90, 19.90, 0, 0, 'PAID', 'WECHAT', 'PAY20260814022', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:45:00'),
('ORD178690000092', 2, '13800138002', 2, 2, '爱奇艺黄金会员月卡', '/uploads/products/img2.png', 22.00, 1, 22.00, 22.00, 0, 0, 'PAID', 'ALIPAY', 'PAY20260814023', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:45:00'),
('ORD178690000093', 3, '13800138003', 3, 3, '瑞幸咖啡29元通兑券', '/uploads/products/img3.png', 19.90, 1, 19.90, 19.90, 0, 0, 'PAID', 'WECHAT', 'PAY20260814024', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:45:00'),
('ORD178690000094', 4, '13800138004', 1, 4, 'QQ音乐绿钻豪华版月卡', '/uploads/products/img4.png', 15.00, 1, 15.00, 15.00, 0, 0, 'PAID', 'ALIPAY', 'PAY20260814025', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:45:00'),
('ORD178690000095', 5, '13800138005', 2, 5, '美团外卖20元红包', '/uploads/products/img5.png', 18.00, 1, 18.00, 18.00, 0, 0, 'PAID', 'WECHAT', 'PAY20260814026', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:45:00'),
('ORD178690000096', 6, '13800138006', 3, 6, '王者荣耀60点券', '/uploads/products/img6.png', 6.00, 1, 6.00, 6.00, 0, 0, 'PAID', 'WECHAT', 'PAY20260814027', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:50:00'),
('ORD178690000097', 7, '13800138007', 1, 7, '网易云音乐黑胶VIP月卡', '/uploads/products/img7.png', 18.00, 1, 18.00, 18.00, 0, 0, 'PAID', 'ALIPAY', 'PAY20260814028', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:45:00'),
('ORD178690000098', 8, '13800138008', 2, 8, '星巴克星礼卡100元', '/uploads/products/img8.png', 95.00, 1, 95.00, 90.00, 5.00, 5.00, 'PAID', 'WECHAT', 'PAY20260814029', NULL, NULL, NULL, '2026-08-14 15:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 14:30:00'),
('ORD178690000099', 1, '13800138001', 1, 9, '麦当劳麦辣鸡腿堡套餐', '/uploads/products/img9.png', 25.00, 1, 25.00, 25.00, 0, 0, 'FULFILLED', 'WECHAT', 'PAY20260814045', '美团配送', 'MT7890123456', '2026-08-14 13:00:00', '2026-08-14 12:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 11:45:00'),
('ORD178690000100', 9, '13800138009', 2, 10, '哔哩哔哩大会员月卡', '/uploads/products/img10.png', 25.00, 2, 50.00, 45.00, 5.00, 5.00, 'EVALUATED', 'ALIPAY', 'PAY20260814046', NULL, NULL, NULL, '2026-08-14 09:00:00', 0, NULL, NULL, NULL, NULL, NULL, '2026-08-14 08:45:00')
ON DUPLICATE KEY UPDATE order_code=order_code;

-- ============================================================
-- 第9部分: 模拟评价数据（对应已完成订单）
-- ============================================================
INSERT INTO evaluation (customer_id, order_id, merchant_id, product_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, merchant_reply, reply_time, create_time) VALUES
(1, 1, 1, 1, '13800138001', 5, 5, 5, 5, 5, '非常好用，秒到账，看视频很流畅！', '到账快,服务好', '感谢您的支持，祝您观影愉快！', '2026-08-02 12:00:00', '2026-08-02 12:00:00'),
(2, 2, 1, 2, '13800138002', 4, 5, 5, 5, 4, '画质清晰，客服态度好，就是价格稍贵', '画质好,客服好', '感谢反馈，我们会持续优化价格策略', '2026-08-02 15:00:00', '2026-08-02 15:00:00'),
(3, 3, 2, 3, '13800138003', 5, 5, 5, 5, 5, '咖啡券太划算了，比直接买便宜很多', '划算,推荐', '谢谢您的认可，欢迎再次购买！', '2026-08-03 17:00:00', '2026-08-03 17:00:00'),
(4, 4, 2, 4, '13800138004', 5, 5, 5, 5, 5, '绿钻会员很不错，音质提升明显', '音质好,推荐', '感谢支持QQ音乐！', '2026-08-03 12:00:00', '2026-08-03 12:00:00'),
(5, 5, 3, 5, '13800138005', 4, 4, 5, 4, 4, '外卖红包很方便，就是面额太小了', '方便,实惠', '感谢使用，我们会推出更多面额选择', '2026-08-03 10:00:00', '2026-08-03 10:00:00'),
(6, 6, 3, 6, '13800138006', 5, 5, 5, 5, 5, '点券秒到，游戏党必备', '秒到账,推荐', '谢谢支持！', '2026-08-04 10:00:00', '2026-08-04 10:00:00'),
(7, 7, 1, 7, '13800138007', 4, 5, 5, 5, 4, '网易云音乐会员，听歌必备', '好用,推荐', '感谢使用网易云音乐', '2026-08-04 15:00:00', '2026-08-04 15:00:00'),
(8, 8, 2, 8, '13800138008', 5, 5, 5, 5, 5, '星礼卡到账快，品质有保障', '品质好,推荐', '感谢您的支持！', '2026-08-05 10:00:00', '2026-08-05 10:00:00'),
(1, 9, 3, 9, '13800138001', 3, 3, 4, 4, 3, '汉堡味道一般，不如肯德基', '一般', '感谢反馈，我们会改进餐品质量', '2026-08-04 12:30:00', '2026-08-04 12:30:00'),
(9, 10, 1, 10, '13800138009', 5, 5, 5, 5, 5, 'B站大会员太值了，新番追不停', '超值,推荐', '感谢支持B站！', '2026-08-04 10:00:00', '2026-08-04 10:00:00'),
(10, 11, 2, 11, '13800138010', 4, 4, 5, 5, 4, '滴滴券打车方便，就是优惠力度不够大', '方便,实用', '我们会推出更多优惠活动', '2026-08-04 15:00:00', '2026-08-04 15:00:00'),
(2, 12, 3, 12, '13800138002', 5, 5, 5, 5, 5, '京东E卡折扣不错，经常来买', '折扣好,推荐', '感谢您的多次支持！', '2026-08-05 10:00:00', '2026-08-05 10:00:00'),
(3, 13, 1, 13, '13800138003', 4, 5, 5, 5, 4, '优酷会员看剧必备', '好用', '谢谢支持！', '2026-08-05 14:00:00', '2026-08-05 14:00:00'),
(4, 14, 2, 14, '13800138004', 5, 5, 5, 5, 5, '专车券很实用，商务出行首选', '实用,推荐', '感谢您的认可！', '2026-08-05 18:00:00', '2026-08-05 18:00:00'),
(5, 15, 3, 15, '13800138005', 4, 5, 5, 5, 4, '翅桶套餐实惠，分量足', '实惠,分量足', '谢谢支持！', '2026-08-06 13:00:00', '2026-08-06 13:00:00'),
(6, 16, 1, 1, '13800138006', 5, 5, 5, 5, 5, '第二次买了，一如既往的好', '老客户,推荐', '感谢老客户的支持！', '2026-08-06 10:00:00', '2026-08-06 10:00:00'),
(7, 17, 2, 2, '13800138007', 4, 5, 5, 5, 4, '爱奇艺会员不错，片源丰富', '片源多', '感谢支持！', '2026-08-06 10:00:00', '2026-08-06 10:00:00'),
(8, 18, 3, 3, '13800138008', 5, 5, 5, 5, 5, '买来送朋友，大家都说好', '送礼佳品,推荐', '感谢您的推荐！', '2026-08-07 14:00:00', '2026-08-07 14:00:00'),
(1, 19, 1, 4, '13800138001', 4, 4, 5, 5, 4, 'QQ音乐会员体验不错', '好用', '谢谢支持！', '2026-08-07 10:00:00', '2026-08-07 10:00:00'),
(9, 20, 3, 5, '13800138009', 5, 5, 5, 5, 5, '外卖红包救急神器', '便捷,推荐', '感谢使用！', '2026-08-07 19:00:00', '2026-08-07 19:00:00'),
(10, 41, 3, 11, '13800138010', 5, 5, 5, 5, 5, '快车券很实用，通勤必备', '实用,推荐', '感谢您的支持！', '2026-08-12 10:00:00', '2026-08-12 10:00:00'),
(2, 42, 1, 12, '13800138002', 4, 5, 5, 5, 4, 'E卡折扣不错，会继续购买', '折扣好', '感谢支持！', '2026-08-13 10:00:00', '2026-08-13 10:00:00'),
(3, 43, 2, 13, '13800138003', 5, 5, 5, 5, 5, '优酷会员观影体验很好', '观影体验好', '谢谢支持！', '2026-08-13 14:00:00', '2026-08-13 14:00:00'),
(4, 44, 3, 14, '13800138004', 4, 4, 5, 5, 4, '专车券服务态度好', '服务好', '感谢您的认可！', '2026-08-13 10:00:00', '2026-08-13 10:00:00'),
(5, 45, 1, 15, '13800138005', 5, 5, 5, 5, 5, '肯德基套餐一如既往的好吃', '好吃,推荐', '谢谢支持肯德基！', '2026-08-14 13:00:00', '2026-08-14 13:00:00'),
(6, 46, 2, 1, '13800138006', 4, 4, 5, 5, 4, '会员续费，稳定可靠', '稳定', '感谢续费支持！', '2026-08-14 10:00:00', '2026-08-14 10:00:00'),
(7, 47, 3, 2, '13800138007', 5, 5, 5, 5, 5, '爱奇艺会员追剧首选', '推荐', '感谢支持！', '2026-08-14 14:00:00', '2026-08-14 14:00:00'),
(8, 48, 1, 3, '13800138008', 4, 5, 5, 5, 4, '咖啡券性价比高', '性价比高', '谢谢支持！', '2026-08-15 14:00:00', '2026-08-15 14:00:00'),
(1, 49, 2, 4, '13800138001', 5, 5, 5, 5, 5, '一次买半年，划算', '划算,推荐', '感谢您的批量购买！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(9, 50, 3, 5, '13800138009', 4, 4, 5, 5, 4, '外卖红包解决午餐问题', '方便', '感谢使用！', '2026-08-15 18:00:00', '2026-08-15 18:00:00'),
(10, 61, 2, 1, '13800138010', 5, 5, 5, 5, 5, '视频会员体验很好，推荐', '推荐', '感谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(2, 62, 3, 2, '13800138002', 4, 5, 5, 5, 4, '爱奇艺片源越来越丰富', '片源丰富', '谢谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(3, 63, 1, 3, '13800138003', 5, 5, 5, 5, 5, '瑞幸咖啡券回购多次', '回购,推荐', '感谢老客户！', '2026-08-15 14:00:00', '2026-08-15 14:00:00'),
(4, 64, 2, 4, '13800138004', 4, 4, 5, 5, 4, 'QQ音乐会员体验不错', '好用', '感谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(5, 65, 3, 5, '13800138005', 5, 5, 5, 5, 5, '美团红包一如既往的优惠', '优惠,推荐', '谢谢支持！', '2026-08-15 19:00:00', '2026-08-15 19:00:00'),
(6, 66, 1, 6, '13800138006', 4, 4, 5, 5, 4, '点券方便快捷', '方便', '感谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(7, 67, 2, 7, '13800138007', 5, 5, 5, 5, 5, '网易云音乐歌单丰富', '歌单丰富,推荐', '感谢支持网易云！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(8, 68, 3, 8, '13800138008', 5, 5, 5, 5, 5, '星巴克品质值得信赖', '品质好,推荐', '感谢您的认可！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(1, 69, 2, 9, '13800138001', 4, 4, 5, 5, 4, '汉堡套餐还不错', '还行', '谢谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(9, 70, 1, 10, '13800138009', 5, 5, 5, 5, 5, 'B站大会员新番看不完', '超值', '感谢支持B站！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(10, 81, 1, 6, '13800138010', 5, 5, 5, 5, 5, '游戏点券充值首选', '首选,推荐', '感谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(2, 82, 2, 7, '13800138002', 4, 4, 5, 5, 4, '网易云音乐音质不错', '音质好', '谢谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(3, 83, 3, 8, '13800138003', 5, 5, 5, 5, 5, '星礼卡送礼首选', '送礼佳品', '感谢您的推荐！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(4, 84, 1, 9, '13800138004', 4, 4, 5, 5, 4, '麦当劳配送快', '配送快', '谢谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(5, 85, 2, 10, '13800138005', 5, 5, 5, 5, 5, 'B站会员看番必备', '必备,推荐', '感谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(6, 86, 3, 11, '13800138006', 4, 4, 5, 5, 4, '滴滴券打车方便', '方便', '感谢使用！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(7, 87, 1, 12, '13800138007', 5, 5, 5, 5, 5, '京东E卡折扣力度大', '折扣大,推荐', '感谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(8, 88, 2, 13, '13800138008', 4, 4, 5, 5, 4, '优酷会员体验不错', '好用', '谢谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(1, 89, 3, 14, '13800138001', 5, 5, 5, 5, 5, '专车券商务出行必备', '必备,推荐', '感谢您的认可！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(9, 90, 1, 15, '13800138009', 4, 5, 5, 5, 4, '肯德基套餐实惠好吃', '实惠,好吃', '谢谢支持！', '2026-08-15 10:00:00', '2026-08-15 10:00:00'),
(9, 100, 2, 10, '13800138009', 5, 5, 5, 5, 5, 'B站大会员体验超棒，推荐', '超棒,推荐', '感谢支持B站！', '2026-08-15 10:00:00', '2026-08-15 10:00:00')
ON DUPLICATE KEY UPDATE order_id=order_id;

-- ============================================================
-- 第10部分: 模拟结算数据（30条，覆盖三类结算类型）
-- ============================================================
INSERT INTO settlement (settle_code, merchant_id, settle_type, settle_period, total_amount, item_count, status, approver, approve_time, create_time) VALUES
('STL20240801001', 1, 'AI_DOU', '2024年7月', 152300.00, 328, 'COMPLETED', 'admin', '2024-08-05 10:30:00', '2024-08-01 09:00:00'),
('STL20240801002', 2, 'AI_DOU', '2024年7月', 98700.00, 215, 'COMPLETED', 'admin', '2024-08-05 14:20:00', '2024-08-01 09:00:00'),
('STL20240801003', 3, 'AI_DOU', '2024年7月', 234500.00, 456, 'COMPLETED', 'admin', '2024-08-06 09:15:00', '2024-08-01 09:00:00'),
('STL20240815001', 1, 'AI_DOU', '2024年8月', 186400.00, 389, 'PENDING', NULL, NULL, '2024-08-15 09:00:00'),
('STL20240815002', 2, 'AI_DOU', '2024年8月', 112300.00, 267, 'PENDING', NULL, NULL, '2024-08-15 09:00:00'),
('STL20240815003', 4, 'AI_DOU', '2024年8月', 76500.00, 143, 'COMPLETED', 'admin', '2024-08-16 11:00:00', '2024-08-15 09:00:00'),
('STL20240820001', 3, 'AI_DOU', '2024年8月', 201200.00, 412, 'PAID', 'admin', '2024-08-21 08:30:00', '2024-08-20 09:00:00'),
('STL20240820002', 5, 'AI_DOU', '2024年8月', 54300.00, 98, 'PENDING', NULL, NULL, '2024-08-20 09:00:00'),
('STL20240822001', 1, 'AI_DOU', '2024年8月', 167800.00, 356, 'COMPLETED', 'admin', '2024-08-23 10:00:00', '2024-08-22 09:00:00'),
('STL20240822002', 2, 'AI_DOU', '2024年8月', 89300.00, 178, 'PAID', 'admin', '2024-08-23 14:00:00', '2024-08-22 09:00:00'),
('STL20240823001', 1, 'AI_DOU', '2024年8月', 213400.00, 445, 'PENDING', NULL, NULL, '2024-08-23 09:00:00'),
('STL20240823002', 3, 'AI_DOU', '2024年8月', 145600.00, 312, 'COMPLETED', 'admin', '2024-08-24 11:00:00', '2024-08-23 09:00:00'),
('STL20240825001', 4, 'AI_DOU', '2024年8月', 98700.00, 189, 'PENDING', NULL, NULL, '2024-08-25 09:00:00'),
('STL20240825002', 2, 'AI_DOU', '2024年8月', 176500.00, 378, 'PAID', 'admin', '2024-08-26 09:00:00', '2024-08-25 09:00:00'),
('STL20240825003', 5, 'AI_DOU', '2024年8月', 65400.00, 123, 'PENDING', NULL, NULL, '2024-08-25 09:00:00')
ON DUPLICATE KEY UPDATE settle_code=settle_code;

INSERT INTO settlement (settle_code, merchant_id, settle_type, settle_period, total_amount, item_count, status, approver, approve_time, create_time) VALUES
('STL20240801004', 1, 'COMMISSION', '2024年7月', 456800.00, 1523, 'COMPLETED', 'admin', '2024-08-05 11:00:00', '2024-08-01 10:00:00'),
('STL20240801005', 2, 'COMMISSION', '2024年7月', 321500.00, 987, 'COMPLETED', 'admin', '2024-08-05 15:00:00', '2024-08-01 10:00:00'),
('STL20240801006', 3, 'COMMISSION', '2024年7月', 567200.00, 2103, 'COMPLETED', 'admin', '2024-08-06 10:00:00', '2024-08-01 10:00:00'),
('STL20240815004', 1, 'COMMISSION', '2024年8月', 523400.00, 1678, 'PENDING', NULL, NULL, '2024-08-15 10:00:00'),
('STL20240815005', 2, 'COMMISSION', '2024年8月', 389200.00, 1123, 'PENDING', NULL, NULL, '2024-08-15 10:00:00'),
('STL20240815006', 4, 'COMMISSION', '2024年8月', 234500.00, 678, 'COMPLETED', 'admin', '2024-08-16 14:00:00', '2024-08-15 10:00:00'),
('STL20240820003', 3, 'COMMISSION', '2024年8月', 612300.00, 1987, 'PAID', 'admin', '2024-08-21 09:00:00', '2024-08-20 10:00:00'),
('STL20240820004', 5, 'COMMISSION', '2024年8月', 178900.00, 456, 'PENDING', NULL, NULL, '2024-08-20 10:00:00'),
('STL20240822003', 1, 'COMMISSION', '2024年8月', 498700.00, 1456, 'COMPLETED', 'admin', '2024-08-23 11:00:00', '2024-08-22 10:00:00'),
('STL20240822004', 2, 'COMMISSION', '2024年8月', 345600.00, 1023, 'PAID', 'admin', '2024-08-23 15:00:00', '2024-08-22 10:00:00')
ON DUPLICATE KEY UPDATE settle_code=settle_code;

INSERT INTO settlement (settle_code, merchant_id, settle_type, settle_period, total_amount, item_count, status, approver, approve_time, create_time) VALUES
('STL20240801007', 1, 'EXPANSION', '2024年7月', 123400.00, 45, 'COMPLETED', 'admin', '2024-08-05 16:00:00', '2024-08-01 11:00:00'),
('STL20240801008', 2, 'EXPANSION', '2024年7月', 98700.00, 32, 'COMPLETED', 'admin', '2024-08-05 17:00:00', '2024-08-01 11:00:00'),
('STL20240801009', 3, 'EXPANSION', '2024年7月', 156700.00, 58, 'COMPLETED', 'admin', '2024-08-06 11:00:00', '2024-08-01 11:00:00'),
('STL20240815007', 1, 'EXPANSION', '2024年8月', 145600.00, 52, 'PENDING', NULL, NULL, '2024-08-15 11:00:00'),
('STL20240815008', 2, 'EXPANSION', '2024年8月', 112300.00, 38, 'PENDING', NULL, NULL, '2024-08-15 11:00:00'),
('STL20240815009', 4, 'EXPANSION', '2024年8月', 67800.00, 21, 'COMPLETED', 'admin', '2024-08-16 16:00:00', '2024-08-15 11:00:00'),
('STL20240820005', 3, 'EXPANSION', '2024年8月', 189200.00, 67, 'PAID', 'admin', '2024-08-21 10:00:00', '2024-08-20 11:00:00'),
('STL20240820006', 5, 'EXPANSION', '2024年8月', 45600.00, 15, 'PENDING', NULL, NULL, '2024-08-20 11:00:00'),
('STL20240822005', 1, 'EXPANSION', '2024年8月', 134500.00, 48, 'COMPLETED', 'admin', '2024-08-23 16:00:00', '2024-08-22 11:00:00'),
('STL20240822006', 2, 'EXPANSION', '2024年8月', 89200.00, 29, 'PAID', 'admin', '2024-08-23 17:00:00', '2024-08-22 11:00:00')
ON DUPLICATE KEY UPDATE settle_code=settle_code;

SET FOREIGN_KEY_CHECKS = 1;

SELECT '============================================' AS '';
SELECT '   All deployment scripts executed successfully!' AS result;
SELECT '============================================' AS '';