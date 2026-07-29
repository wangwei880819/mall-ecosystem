-- ================================================================
-- 商城生态运营系统 MySQL DDL
-- ================================================================

CREATE DATABASE IF NOT EXISTS igou_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE igou_mall;

-- ========== 1. 系统用户表 ==========
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(128) COMMENT '邮箱',
    role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色：SUPER_ADMIN/SYS_ADMIN/OPERATOR/AUDITOR/USER',
    platforms VARCHAR(256) COMMENT '可登录平台ID（逗号分隔）',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/FROZEN/DELETED',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) COMMENT '最后登录IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ========== 2. 商户表 ==========
CREATE TABLE merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    merchant_code VARCHAR(32) NOT NULL COMMENT '商户编号',
    merchant_name VARCHAR(128) NOT NULL COMMENT '企业名称',
    merchant_type VARCHAR(32) NOT NULL COMMENT '类型：DIGITAL/PHYSICAL/LOCAL_LIFE',
    credit_code VARCHAR(64) COMMENT '统一社会信用代码',
    legal_person VARCHAR(64) COMMENT '法定代表人',
    registered_capital VARCHAR(64) COMMENT '注册资本',
    business_scope TEXT COMMENT '经营范围',
    contact_name VARCHAR(64) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    onboarding_step INT NOT NULL DEFAULT 1 COMMENT '入驻当前环节（1-8）',
    onboarding_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '入驻状态：PENDING/REVIEWING/APPROVED/REJECTED',
    risk_level VARCHAR(16) DEFAULT 'LOW' COMMENT '风险等级：LOW/MEDIUM/HIGH',
    merchant_grade VARCHAR(8) DEFAULT 'B' COMMENT '商户等级：A/B/C/D',
    commission_rate DECIMAL(5,4) DEFAULT 0.0500 COMMENT '佣金比例',
    settle_account VARCHAR(64) COMMENT '结算账户',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_merchant_code (merchant_code),
    KEY idx_type (merchant_type),
    KEY idx_status (onboarding_status),
    KEY idx_risk (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

-- ========== 3. 商品表 ==========
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_code VARCHAR(32) NOT NULL COMMENT '商品编号',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    category VARCHAR(64) NOT NULL COMMENT '品类',
    brand VARCHAR(64) COMMENT '品牌',
    merchant_id BIGINT NOT NULL COMMENT '所属商户ID',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    market_price DECIMAL(10,2) COMMENT '市场价',
    stock INT DEFAULT 0 COMMENT '库存',
    sales_count INT DEFAULT 0 COMMENT '销量',
    avg_score DECIMAL(3,1) DEFAULT 0.0 COMMENT '平均评分',
    ai_selling_point TEXT COMMENT 'AI生成的卖点文案',
    ai_tag VARCHAR(64) COMMENT 'AI状态标签',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/ON_SHELF/OFF_SHELF/REJECTED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_merchant (merchant_id),
    KEY idx_category (category),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ========== 4. 订单表 ==========
CREATE TABLE mall_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    order_amount DECIMAL(12,2) NOT NULL COMMENT '订单金额',
    ai_dou_deduct DECIMAL(12,2) DEFAULT 0.00 COMMENT 'AI豆抵扣金额',
    pay_amount DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT '状态：CREATED/PAID/FULFILLED/EVALUATED/REFUNDED',
    pay_time DATETIME COMMENT '支付时间',
    fulfill_time DATETIME COMMENT '履约时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_code (order_code),
    KEY idx_merchant (merchant_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ========== 5. 结算表 ==========
CREATE TABLE settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    settle_code VARCHAR(32) NOT NULL COMMENT '结算编号',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    settle_type VARCHAR(32) NOT NULL COMMENT '结算类型：AI_DOU/COMMISSION/EXPANSION',
    settle_period VARCHAR(32) NOT NULL COMMENT '结算周期',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '结算总金额',
    item_count INT NOT NULL DEFAULT 0 COMMENT '结算笔数',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/CHECKING/APPROVED/COMPLETED',
    approver VARCHAR(64) COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_settle_code (settle_code),
    KEY idx_merchant (merchant_id),
    KEY idx_type (settle_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算表';

-- ========== 6. 稽核表 ==========
CREATE TABLE audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    audit_code VARCHAR(32) NOT NULL COMMENT '稽核编号',
    audit_type VARCHAR(32) NOT NULL COMMENT '稽核类型：ORDER/FUND',
    target_code VARCHAR(32) COMMENT '关联订单/结算编号',
    merchant_id BIGINT COMMENT '商户ID',
    risk_type VARCHAR(64) COMMENT '风险类型',
    risk_level VARCHAR(16) NOT NULL DEFAULT 'LOW' COMMENT '风险等级：LOW/MEDIUM/HIGH',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '涉及金额',
    description TEXT COMMENT '风险描述',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/CHECKING/RESOLVED',
    handler VARCHAR(64) COMMENT '处理人',
    handle_time DATETIME COMMENT '处理时间',
    handle_result TEXT COMMENT '处理结果',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_audit_code (audit_code),
    KEY idx_type (audit_type),
    KEY idx_risk (risk_level),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='稽核记录表';

-- ========== 7. 评价表 ==========
CREATE TABLE evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    score_quality TINYINT NOT NULL DEFAULT 5 COMMENT '商品质量评分1-5',
    score_delivery TINYINT NOT NULL DEFAULT 5 COMMENT '配送速度评分',
    score_service TINYINT NOT NULL DEFAULT 5 COMMENT '客服服务评分',
    score_aftersale TINYINT NOT NULL DEFAULT 5 COMMENT '售后体验评分',
    score_value TINYINT NOT NULL DEFAULT 5 COMMENT '性价比评分',
    content TEXT COMMENT '评价内容',
    tags VARCHAR(256) COMMENT '评价标签（逗号分隔）',
    sentiment VARCHAR(16) DEFAULT 'POSITIVE' COMMENT '情感：POSITIVE/NEUTRAL/NEGATIVE',
    ai_status VARCHAR(32) DEFAULT 'AUTO_PASS' COMMENT 'AI审核状态：AUTO_PASS/MANUAL_REVIEW/BLOCKED',
    merchant_reply TEXT COMMENT '商户回复',
    reply_time DATETIME COMMENT '回复时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order (order_id),
    KEY idx_merchant (merchant_id),
    KEY idx_sentiment (sentiment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ========== 8. AI任务表 ==========
CREATE TABLE ai_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_type VARCHAR(32) NOT NULL COMMENT '任务类型：OCR/ENTRY/PROOFREAD/CONTRACT/PRICE/SELLING',
    biz_type VARCHAR(32) COMMENT '业务类型：MERCHANT/PRODUCT',
    biz_id BIGINT COMMENT '业务ID',
    input_data TEXT COMMENT '输入数据JSON',
    output_data TEXT COMMENT '输出数据JSON',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/COMPLETED/FAILED',
    process_time_ms INT COMMENT '处理耗时（毫秒）',
    model_name VARCHAR(64) COMMENT '使用的AI模型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_type (task_type),
    KEY idx_biz (biz_type, biz_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI任务表';

-- ========== 9. 接口配置表 ==========
CREATE TABLE api_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    api_name VARCHAR(64) NOT NULL COMMENT '接口名称',
    api_path VARCHAR(256) NOT NULL COMMENT '接口路径',
    target_system VARCHAR(64) NOT NULL COMMENT '目标系统',
    protocol VARCHAR(16) NOT NULL DEFAULT 'HTTP' COMMENT '协议',
    auth_type VARCHAR(32) NOT NULL DEFAULT 'JWT' COMMENT '认证方式：JWT/OAUTH/API_KEY',
    rate_limit INT DEFAULT 100 COMMENT '限流阈值（次/秒）',
    timeout_ms INT DEFAULT 5000 COMMENT '超时时间（毫秒）',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api_path (api_path),
    KEY idx_target (target_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口配置表';

-- ========== 种子数据 ==========
INSERT INTO sys_user (username, password, real_name, phone, role, platforms, status) VALUES
('admin', 'demo123', '系统管理员', '13800138000', 'SUPER_ADMIN', '1,2,3,4,5,6,7', 'ACTIVE'),
('operator01', 'demo123', '运营专员', '13800138001', 'OPERATOR', '1,2,3', 'ACTIVE'),
('auditor01', 'demo123', '稽核专员', '13800138002', 'AUDITOR', '1,3,5', 'ACTIVE');

INSERT INTO merchant (merchant_code, merchant_name, merchant_type, credit_code, legal_person, contact_name, contact_phone, onboarding_step, onboarding_status, risk_level, commission_rate) VALUES
('M20240823001', '瑞幸咖啡（中国）有限公司', 'DIGITAL', '91110108MA01ABC23X', '郭谨一', '张经理', '13900001111', 3, 'REVIEWING', 'LOW', 0.0500),
('M20240823002', '上海寻梦信息技术有限公司', 'PHYSICAL', '91310000MA1FABC45', '陈磊', '李总', '13900002222', 8, 'APPROVED', 'LOW', 0.0800),
('M20240823003', '深圳腾讯计算机系统有限公司', 'DIGITAL', '91440300MA1FABC67', '马化腾', '王总监', '13900003333', 8, 'APPROVED', 'LOW', 0.0300),
('M20240823004', '阿里巴巴（中国）有限公司', 'PHYSICAL', '91330100MA1FABC89', '张勇', '赵经理', '13900004444', 5, 'REVIEWING', 'MEDIUM', 0.0600);

INSERT INTO product (product_code, product_name, category, brand, merchant_id, price, market_price, stock, sales_count, avg_score, ai_selling_point, status) VALUES
('PRD20240823001', '腾讯视频VIP会员月卡', '视频娱乐', '腾讯视频', 3, 19.90, 30.00, 1000, 568, 4.8, '🔥 腾讯视频VIP月卡，低至6.6折！追剧不等待，大片随心看！4K超清+无广告+多设备同登，一站式满足全家观影需求。', 'ON_SHELF'),
('PRD20240823002', '爱奇艺黄金会员月卡', '视频娱乐', '爱奇艺', 3, 22.00, 30.00, 800, 432, 4.7, '🎬 爱奇艺黄金VIP月卡，院线大片抢先看！4K超清画质，沉浸式观影体验，纯净无广告！', 'ON_SHELF'),
('PRD20240823003', '瑞幸咖啡29元饮品通兑券', '餐饮美食', '瑞幸咖啡', 1, 19.90, 29.00, 5000, 1256, 4.9, '☕ 瑞幸咖啡超值通兑券！全场饮品通用，新鲜现做，门店自提，咖啡自由即刻拥有！', 'ON_SHELF'),
('PRD20240823004', '优酷VIP会员季卡', '视频娱乐', '优酷', 3, 45.00, 68.00, 600, 234, 4.6, '📺 优酷VIP季卡，热门综艺看不停！独播大剧、院线大片、品质综艺一站式享！', 'ON_SHELF'),
('PRD20240823005', '芒果TV会员月卡', '视频娱乐', '芒果TV', 3, 18.80, 25.00, 750, 345, 4.5, '🥭 芒果TV会员月卡，热门综艺随心看！《乘风破浪》《向往的生活》等热门综艺独家追！', 'ON_SHELF');

INSERT INTO mall_order (order_code, merchant_id, product_id, user_phone, quantity, order_amount, ai_dou_deduct, pay_amount, status) VALUES
('ORD202408230001', 3, 1, '13812345678', 1, 19.90, 2.00, 17.90, 'EVALUATED'),
('ORD202408230002', 1, 3, '13812345679', 2, 39.80, 5.00, 34.80, 'PAID'),
('ORD202408230003', 3, 2, '13812345680', 1, 22.00, 0.00, 22.00, 'FULFILLED');

INSERT INTO evaluation (order_id, merchant_id, product_id, user_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status) VALUES
(1, 3, 1, '13812345678', 5, 5, 5, 5, 5, '非常好用！开通秒到账，没有广告太爽了！', '快速到账,无广告,画质清晰', 'POSITIVE', 'AUTO_PASS'),
(2, 1, 3, '13812345679', 4, 5, 4, 4, 5, '咖啡很好喝，兑换也很方便', '口感好,兑换便捷', 'POSITIVE', 'AUTO_PASS');

INSERT INTO settlement (settle_code, merchant_id, settle_type, settle_period, total_amount, item_count, status) VALUES
('STL20240823001', 3, 'COMMISSION', '2024-08', 8560.00, 128, 'COMPLETED'),
('STL20240823002', 1, 'AI_DOU', '2024-08', 3200.00, 56, 'APPROVED'),
('STL20240823003', 2, 'COMMISSION', '2024-08', 12580.00, 234, 'PENDING');

INSERT INTO audit_record (audit_code, audit_type, target_code, merchant_id, risk_type, risk_level, amount, description, status) VALUES
('AUD20240823001', 'ORDER', 'ORD202408230002', 1, '刷单嫌疑', 'MEDIUM', 34.80, '同一IP短时间内多次下单', 'PENDING'),
('AUD20240823002', 'FUND', 'STL20240823003', 2, '金额波动异常', 'HIGH', 12580.00, '结算金额较上月增长250%', 'CHECKING');

INSERT INTO api_config (api_name, api_path, target_system, auth_type, rate_limit) VALUES
('4A统一认证', '/api/auth/sso', '4A_SYSTEM', 'OAUTH', 200),
('OCR资质识别', '/api/ai/ocr', 'AI_PLATFORM', 'JWT', 50),
('价格采集', '/api/ai/price-collect', 'AI_PLATFORM', 'JWT', 30),
('支付收银', '/api/pay/cashier', 'BASE_CAPABILITY', 'JWT', 500),
('订单同步', '/api/order/sync', 'BASE_CAPABILITY', 'JWT', 500);

-- ========== SSO接入平台表 ==========
CREATE TABLE IF NOT EXISTS sso_platform (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    system_code VARCHAR(64),
    auth_type VARCHAR(32) DEFAULT 'OAUTH',
    icon VARCHAR(16),
    url VARCHAR(256),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sso_platform (id, name, system_code, auth_type, icon, url, status) VALUES
(1, '生态合作平台', 'ECO_PLATFORM', 'OAUTH', '🏢', NULL, 'ACTIVE'),
(2, '积分商城后台', 'POINTS_MALL', 'JWT', '🎁', NULL, 'ACTIVE'),
(3, '权益超市后台', 'BENEFIT_MART', 'JWT', '🎬', NULL, 'ACTIVE'),
(4, '泛全联盟平台', 'ALLIANCE', 'OAUTH', '🌐', NULL, 'ACTIVE'),
(5, '风控稽核管理平台', 'RISK_AUDIT', 'JWT', '🛡️', 'http://localhost:3001', 'ACTIVE'),
(6, '工单管理系统', 'WORK_ORDER', 'JWT', '🎫', NULL, 'ACTIVE'),
(7, '评价管理系统', 'EVALUATION', 'JWT', '⭐', NULL, 'ACTIVE');
