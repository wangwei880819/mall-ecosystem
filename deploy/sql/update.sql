-- ============================================================
-- 商城生态运营管理平台 — 数据库更新脚本
-- 生成时间: 2026-08-14
-- 用途: 补齐测试环境缺失表 + 导入风控相关种子数据
-- 执行: mysql -u igou -p igou_mall < update.sql
-- ============================================================

-- ============================================================
-- 第1部分: 补齐缺失表（本地环境有，schema.sql缺失）
-- ============================================================

-- 1. 轮播图表
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

-- 2. 客户行为记录表
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

-- 3. 客户标签表
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

-- 4. 收货地址表
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

-- 5. 首页配置表
CREATE TABLE IF NOT EXISTS home_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) DEFAULT '' COMMENT '配置值',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页配置';

-- 6. 发票表
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

-- 7. 商户资质表
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

-- 8. 商品分类表
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

-- 9. 对账记录表
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

-- 10. 退款申请表
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

-- 11. 结算明细表
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

-- 12. 库存变动记录表
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

-- 13. 系统日志表
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

-- 14. 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT,
    description VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

-- 15. 交易监控表
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

-- 2.1 黑名单数据（名单库）
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

-- 2.2 风控事件数据（风控事件/看板/稽核日志）
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

-- 2.3 风控规则数据（规则管理）
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

-- 2.4 处置方案数据（处置管理）
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

-- 完成
-- ============================================================

-- ============================================================
-- 第3部分: product 表新增审核字段（幂等：列已存在时跳过）
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

CALL safe_add_column('product', 'review_level', 'INT DEFAULT NULL COMMENT ''审核级别：null=待一级审核, 1=待二级审核, 2=已通过''');
CALL safe_add_column('product', 'level1_audit_time', 'DATETIME DEFAULT NULL COMMENT ''一级审核时间''');
CALL safe_add_column('product', 'level1_auditor', 'VARCHAR(64) DEFAULT NULL COMMENT ''一级审核人''');
CALL safe_add_column('product', 'merchant_name', 'VARCHAR(128) DEFAULT NULL COMMENT ''商户名称''');
CALL safe_add_column('product', 'vip_price', 'DECIMAL(10,2) DEFAULT NULL COMMENT ''VIP价格''');
CALL safe_add_column('product', 'update_time', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''');

CALL safe_add_column('commission_config', 'settle_type', 'VARCHAR(32) COMMENT ''结算类型：AI_DOU/COMMISSION/EXPANSION''');
CALL safe_add_column('commission_config', 'settle_period', 'VARCHAR(32) DEFAULT ''MONTHLY'' COMMENT ''结算周期：MONTHLY/WEEKLY/DAILY''');
CALL safe_add_column('commission_config', 'min_settle_amount', 'DECIMAL(12,2) DEFAULT 100.00 COMMENT ''最低结算额''');

-- 评价表 evaluation：处理 user_phone→customer_phone 重命名 + 补齐缺失列
SET @has_user_phone = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'evaluation' AND column_name = 'user_phone');
SET @has_customer_phone = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'evaluation' AND column_name = 'customer_phone');
SET @rename_sql = IF(@has_user_phone > 0 AND @has_customer_phone = 0, 'ALTER TABLE evaluation CHANGE COLUMN user_phone customer_phone VARCHAR(20) COMMENT ''客户手机号''', 'SELECT ''rename: skip'' AS msg');
PREPARE rename_stmt FROM @rename_sql;
EXECUTE rename_stmt;
DEALLOCATE PREPARE rename_stmt;

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

DROP PROCEDURE IF EXISTS safe_add_column;

-- ============================================================
-- 第4部分: 补充系统管理菜单 - 日志管理
-- ============================================================
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, sort_order, visible, keep_alive, status) 
SELECT 48, 8, '日志管理', 'MENU', '/system/logs', 'system/LogManage', '📋', 5, 1, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 48);

-- 为超级管理员角色授权日志管理菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, 48 FROM sys_role r WHERE r.role_code = 'SUPER_ADMIN'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = 48);

SELECT 'Database update completed!' AS result;