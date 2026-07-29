-- =====================================================
-- 风控稽核管理平台 数据库脚本
-- =====================================================

-- 风控事件表
DROP TABLE IF EXISTS risk_event;
CREATE TABLE risk_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    target VARCHAR(200) COMMENT '目标对象',
    risk_level VARCHAR(20) COMMENT '风险等级',
    score INT COMMENT '风险评分',
    hit_rule VARCHAR(100) COMMENT '命中规则',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    source VARCHAR(100) COMMENT '来源',
    detail VARCHAR(500) COMMENT '事件详情',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控事件表';

-- 风控规则表
DROP TABLE IF EXISTS risk_check_rule;
CREATE TABLE risk_check_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    type VARCHAR(50) COMMENT '规则类型',
    scene VARCHAR(50) COMMENT '适用场景',
    priority INT DEFAULT 5 COMMENT '优先级',
    rule_condition VARCHAR(500) COMMENT '触发条件',
    action VARCHAR(50) COMMENT '处置动作',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    description VARCHAR(500) COMMENT '规则描述',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- 黑白名单表
DROP TABLE IF EXISTS blacklist_item;
CREATE TABLE blacklist_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(200) NOT NULL COMMENT '名单值',
    type VARCHAR(50) COMMENT '数据类型',
    list_type VARCHAR(20) COMMENT '名单类型',
    reason VARCHAR(500) COMMENT '加入原因',
    source VARCHAR(100) COMMENT '来源',
    operator VARCHAR(100) COMMENT '操作人',
    create_time DATETIME COMMENT '创建时间',
    expire_time DATETIME COMMENT '过期时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑白名单表';

-- 处置方案表
DROP TABLE IF EXISTS disposal_config;
CREATE TABLE disposal_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '方案名称',
    type VARCHAR(50) COMMENT '处置方式',
    trigger_rule VARCHAR(200) COMMENT '触发规则',
    risk_level VARCHAR(20) COMMENT '适用风险等级',
    duration VARCHAR(50) COMMENT '持续时间',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    exec_count INT DEFAULT 0 COMMENT '执行次数',
    description VARCHAR(500) COMMENT '处置说明',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处置方案表';

-- =====================================================
-- 测试数据: 风控事件 (8条)
-- =====================================================
INSERT INTO risk_event (event_type, target, risk_level, score, hit_rule, status, source, detail, create_time, update_time) VALUES
('下单风控', '用户138****8001 / 商户:数码旗舰店', 'HIGH', 92, '订单金额异常', 'PENDING', '生态合作平台', '用户单笔订单金额超过历史平均值500%，且收货地址为近期新增地址', '2026-07-27 10:32:15', '2026-07-27 10:32:15'),
('注册风控', 'IP:192.168.1.100', 'MEDIUM', 65, '注册频率异常', 'BLOCKED', '生态合作平台', '同一IP地址30分钟内注册超过10个账号', '2026-07-27 10:28:40', '2026-07-27 10:28:40'),
('支付风控', '用户139****8002 / 订单:ORD20260727003', 'HIGH', 88, '异地支付检测', 'PENDING', '生态合作平台', '用户登录地和支付IP地址跨省，且支付金额较大', '2026-07-27 10:15:22', '2026-07-27 10:15:22'),
('评价风控', '商户:美妆集合店 / 商品SPU0001', 'LOW', 35, '评价内容敏感', 'PASSED', '生态合作平台', '评价内容包含敏感词，但经分析为正常评价', '2026-07-27 09:58:03', '2026-07-27 09:58:03'),
('退款风控', '用户137****8003', 'MEDIUM', 72, '退款频率异常', 'PENDING', '生态合作平台', '该用户近7天退款率超过60%，疑似恶意退款', '2026-07-27 09:45:11', '2026-07-27 09:45:11'),
('登录风控', '设备指纹:DEV_UNKNOWN_001', 'HIGH', 85, '设备指纹异常', 'MANUAL', '生态合作平台', '检测到设备指纹模拟器特征，疑似使用虚拟设备', '2026-07-27 09:30:55', '2026-07-27 09:30:55'),
('下单风控', '商户:本地生活馆', 'MEDIUM', 58, '价格异常波动', 'PENDING', '生态合作平台', '商品价格在1小时内变动超过200%，疑似价格操纵', '2026-07-27 09:15:28', '2026-07-27 09:15:28'),
('入驻风控', '商户申请:XX科技有限公司', 'HIGH', 90, '资质材料异常', 'PENDING', '生态合作平台', '营业执照图片存在PS痕迹，统一社会信用代码在黑名单中', '2026-07-27 08:50:12', '2026-07-27 08:50:12');

-- =====================================================
-- 测试数据: 风控规则 (10条)
-- =====================================================
INSERT INTO risk_check_rule (name, type, scene, priority, rule_condition, action, hit_count, active, description, create_time, update_time) VALUES
('订单金额异常检测', 'CONDITION', '下单风控', 1, 'order.amount > avgAmount * 5 AND user.accountAge < 7天', 'BLOCK', 3250, 1, '检测订单金额是否远超历史均值', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('设备指纹异常检测', 'SCRIPT', '登录风控', 2, 'device.fingerprint in virtualDeviceList OR device.emulator == true', 'BLOCK', 2890, 1, '检测异常设备指纹和模拟器特征', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('注册频率限制', 'FREQUENCY', '注册风控', 3, 'register.count > 10 PER 30min FROM sameIP', 'BLOCK', 2150, 1, '限制同IP高频注册行为', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('退款频率异常', 'FREQUENCY', '退款风控', 4, 'refund.rate > 60% AND refund.count > 5 PER 7天', 'MANUAL', 1820, 1, '检测异常退款行为模式', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('商品价格异常波动', 'CONDITION', '下单风控', 5, 'product.price.change > 200% PER 1h', 'DOWNGRADE', 1560, 1, '检测商品价格异常波动', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('评价内容敏感词过滤', 'SCRIPT', '评价风控', 6, 'review.content contains sensitiveKeywords', 'MANUAL', 1280, 1, '过滤评价中的敏感内容', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('商户资质材料异常', 'SCRIPT', '入驻风控', 1, 'merchant.creditCode in blacklist OR businessLicense.PS_Score > 0.8', 'BLOCK', 980, 1, '检测商户资质造假风险', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('异地支付检测', 'RELATION', '支付风控', 2, 'user.loginProvince != pay.ipProvince AND order.amount > 500', 'MANUAL', 750, 1, '检测异地大额支付行为', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('账号关联检测', 'RELATION', '注册风控', 3, 'accounts linked by device OR ip OR payment', 'DOWNGRADE', 520, 1, '检测多账号关联关系', '2026-07-01 00:00:00', '2026-07-27 10:00:00'),
('优惠券滥用检测', 'FREQUENCY', '下单风控', 4, 'coupon.use.count > 20 PER 1天 FROM sameAccount', 'ALERT', 380, 0, '检测优惠券异常使用行为', '2026-07-01 00:00:00', '2026-07-27 10:00:00');

-- =====================================================
-- 测试数据: 黑白名单 (8条)
-- =====================================================
INSERT INTO blacklist_item (value, type, list_type, reason, source, operator, create_time, expire_time) VALUES
('138****1234', 'PHONE', 'BLACK', '多次恶意退款，累计退款率85%', '系统自动', '风控系统', '2026-07-20 14:30:00', '2027-07-20 14:30:00'),
('192.168.1.200', 'IP', 'BLACK', '批量注册攻击源IP', '安全组', '管理员', '2026-07-18 09:15:00', NULL),
('DEV_EMU_20260701', 'DEVICE', 'BLACK', '模拟器设备指纹，关联多个欺诈账号', '系统自动', '风控系统', '2026-07-15 16:45:00', NULL),
('91110108MA01XXXXX', 'CREDIT_CODE', 'BLACK', '营业执照造假，已被市场监管部门列入异常', '外部数据', '审核员', '2026-07-10 11:20:00', NULL),
('MER20260001', 'MERCHANT', 'BLACK', '多次违规经营，已被平台清退', '运营组', '管理员', '2026-07-05 08:00:00', NULL),
('139****5678', 'PHONE', 'WHITE', '平台VIP大客户', '运营组', '管理员', '2026-06-01 10:00:00', NULL),
('10.0.0.100', 'IP', 'WHITE', '公司内部测试IP', '技术组', '管理员', '2026-05-15 09:00:00', NULL),
('137****9012', 'PHONE', 'GRAY', '行为异常监测中，退款率45%', '系统自动', '风控系统', '2026-07-25 14:00:00', '2026-08-25 14:00:00');

-- =====================================================
-- 测试数据: 处置方案 (8条)
-- =====================================================
INSERT INTO disposal_config (name, type, trigger_rule, risk_level, duration, status, exec_count, description, create_time, update_time) VALUES
('高风险订单自动拦截', 'BLOCK', '订单金额异常检测', 'HIGH', '24小时', 'ACTIVE', 3250, '对高风险订单进行自动拦截处理', '2026-07-01 00:00:00', '2026-07-25 14:00:00'),
('异常设备登录拦截', 'BLOCK', '设备指纹异常检测', 'HIGH', '永久', 'ACTIVE', 2890, '拦截异常设备的登录请求', '2026-07-01 00:00:00', '2026-07-25 10:00:00'),
('退款异常人工审核', 'MANUAL', '退款频率异常', 'MEDIUM', '7天', 'ACTIVE', 1820, '对异常退款行为转人工审核', '2026-07-01 00:00:00', '2026-07-24 16:30:00'),
('价格操纵降权处理', 'DOWNGRADE', '商品价格异常波动', 'MEDIUM', '30天', 'ACTIVE', 1560, '对价格操纵商户进行降权处理', '2026-07-01 00:00:00', '2026-07-24 11:00:00'),
('资质造假冻结账户', 'FREEZE', '商户资质材料异常', 'HIGH', '永久', 'ACTIVE', 980, '冻结资质造假商户的账户', '2026-07-01 00:00:00', '2026-07-23 09:00:00'),
('异地支付安全审核', 'MANUAL', '异地支付检测', 'HIGH', '24小时', 'ACTIVE', 750, '对异地大额支付进行安全审核', '2026-07-01 00:00:00', '2026-07-22 15:00:00'),
('敏感评价Webhook通知', 'WEBHOOK', '评价内容敏感词过滤', 'LOW', '1小时', 'ACTIVE', 1280, '通过Webhook通知运营处理敏感评价', '2026-07-01 00:00:00', '2026-07-21 10:00:00'),
('批量注册限制登录', 'LOGIN_LIMIT', '注册频率限制', 'HIGH', '7天', 'INACTIVE', 2150, '限制批量注册账号的登录权限', '2026-07-01 00:00:00', '2026-07-20 08:00:00');
