-- ================================================================
-- 商城生态运营系统 MySQL 数据库初始化（演示用）
-- ================================================================

-- ========== 1. 系统用户表 ==========
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(128),
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    platforms VARCHAR(256),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    last_login_time DATETIME,
    last_login_ip VARCHAR(64),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
);

-- ========== 2. 商户表 ==========
CREATE TABLE IF NOT EXISTS merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_code VARCHAR(32) NOT NULL,
    merchant_name VARCHAR(128) NOT NULL,
    merchant_type VARCHAR(32) NOT NULL,
    credit_code VARCHAR(64),
    legal_person VARCHAR(64),
    registered_capital VARCHAR(64),
    business_scope VARCHAR(2000),
    contact_name VARCHAR(64),
    contact_phone VARCHAR(20),
    onboarding_step INT NOT NULL DEFAULT 1,
    onboarding_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    risk_level VARCHAR(16) DEFAULT 'LOW',
    merchant_grade VARCHAR(8) DEFAULT 'B',
    commission_rate DECIMAL(5,4) DEFAULT 0.0500,
    settle_account VARCHAR(64),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_merchant_code (merchant_code)
);

-- ========== 3. 商品表 ==========
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(32) NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    product_type VARCHAR(32) DEFAULT 'PHYSICAL' COMMENT '商品类型: PHYSICAL/VIRTUAL/BENEFIT',
    category_id BIGINT,
    category VARCHAR(64) NOT NULL,
    brand VARCHAR(64),
    merchant_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    market_price DECIMAL(10,2),
    stock INT DEFAULT 0,
    sales_count INT DEFAULT 0,
    avg_score DECIMAL(3,1) DEFAULT 0.0,
    ai_selling_point VARCHAR(2000),
    ai_tag VARCHAR(64),
    description VARCHAR(2000),
    detail MEDIUMTEXT,
    image_urls VARCHAR(2000),
    spec VARCHAR(128),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_code (product_code)
);

-- ========== 4. 订单表 ==========
CREATE TABLE IF NOT EXISTS mall_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(32) NOT NULL,
    customer_id BIGINT,
    customer_phone VARCHAR(20),
    merchant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(256),
    product_image VARCHAR(512),
    price DECIMAL(10,2),
    quantity INT NOT NULL DEFAULT 1,
    order_amount DECIMAL(12,2) NOT NULL,
    ai_dou_deduct DECIMAL(12,2) DEFAULT 0.00,
    pay_amount DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0.00,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    pay_method VARCHAR(32),
    pay_time DATETIME,
    pay_no VARCHAR(64),
    delivery_address_id BIGINT,
    logistics_no VARCHAR(64),
    logistics_company VARCHAR(128),
    fulfill_time DATETIME,
    refund_amount DECIMAL(12,2),
    refund_time DATETIME,
    refund_reason VARCHAR(512),
    cancel_reason VARCHAR(512),
    cancel_time DATETIME,
    remark VARCHAR(512),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_code (order_code)
);

-- ========== 5. 结算表 ==========
CREATE TABLE IF NOT EXISTS settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settle_code VARCHAR(32) NOT NULL,
    merchant_id BIGINT NOT NULL,
    settle_type VARCHAR(32) NOT NULL,
    settle_period VARCHAR(32) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    item_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    approver VARCHAR(64),
    approve_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_settle_code (settle_code)
);

-- ========== 6. 稽核表 ==========
CREATE TABLE IF NOT EXISTS audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    audit_code VARCHAR(32) NOT NULL,
    audit_type VARCHAR(32) NOT NULL,
    target_code VARCHAR(32),
    merchant_id BIGINT,
    risk_type VARCHAR(64),
    risk_level VARCHAR(16) NOT NULL DEFAULT 'LOW',
    amount DECIMAL(12,2) DEFAULT 0.00,
    description VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    handler VARCHAR(64),
    handle_time DATETIME,
    handle_result VARCHAR(2000),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_audit_code (audit_code)
);

-- ========== 7. 评价表 ==========
CREATE TABLE IF NOT EXISTS evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_phone VARCHAR(20),
    score_quality TINYINT NOT NULL DEFAULT 5,
    score_delivery TINYINT NOT NULL DEFAULT 5,
    score_service TINYINT NOT NULL DEFAULT 5,
    score_aftersale TINYINT NOT NULL DEFAULT 5,
    score_value TINYINT NOT NULL DEFAULT 5,
    content VARCHAR(2000),
    tags VARCHAR(256),
    sentiment VARCHAR(16) DEFAULT 'POSITIVE',
    ai_status VARCHAR(32) DEFAULT 'AUTO_PASS',
    merchant_reply VARCHAR(2000),
    reply_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== 8. AI任务表 ==========
CREATE TABLE IF NOT EXISTS ai_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_type VARCHAR(32) NOT NULL,
    biz_type VARCHAR(32),
    biz_id BIGINT,
    input_data VARCHAR(2000),
    output_data VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    process_time_ms INT,
    model_name VARCHAR(64),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== 9. 客户表 ==========
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(128) NOT NULL,
    nickname VARCHAR(64),
    vip_level VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    register_time DATETIME,
    last_login_time DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone (phone)
);

-- ========== 10. 客户地址表 ==========
CREATE TABLE IF NOT EXISTS customer_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(64),
    city VARCHAR(64),
    district VARCHAR(64),
    detail VARCHAR(256),
    is_default INT DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== 11. 购物车表 ==========
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(128),
    product_image VARCHAR(512),
    product_price DECIMAL(10,2),
    quantity INT NOT NULL DEFAULT 1,
    selected INT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== 12. 订单明细表 ==========
CREATE TABLE IF NOT EXISTS mall_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(128),
    product_image VARCHAR(512),
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    item_amount DECIMAL(12,2) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS api_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_name VARCHAR(64) NOT NULL,
    api_path VARCHAR(256) NOT NULL,
    target_system VARCHAR(64) NOT NULL,
    protocol VARCHAR(16) NOT NULL DEFAULT 'HTTP',
    auth_type VARCHAR(32) NOT NULL DEFAULT 'JWT',
    rate_limit INT DEFAULT 100,
    timeout_ms INT DEFAULT 5000,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api_path (api_path)
);

-- ========== 权益商品表 ==========
CREATE TABLE IF NOT EXISTS benefit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    benefit_code VARCHAR(32) NOT NULL COMMENT '权益编号',
    benefit_name VARCHAR(128) NOT NULL COMMENT '权益名称',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    benefit_type VARCHAR(32) NOT NULL COMMENT '权益类型: MEMBERSHIP/COUPON/GAME_POINTS/DIGITAL_CONTENT/SERVICE/INSURANCE',
    face_value DECIMAL(10,2) COMMENT '面值/原价',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    settle_price DECIMAL(10,2) COMMENT '结算价',
    validity_type VARCHAR(32) COMMENT '有效期类型: FIXED_DATE/DAYS_AFTER_RECEIVE/DURATION',
    validity_start DATETIME COMMENT '有效期开始',
    validity_end DATETIME COMMENT '有效期结束',
    validity_days INT COMMENT '有效天数',
    usage_rules VARCHAR(2000) COMMENT '使用规则',
    applicable_scope VARCHAR(500) COMMENT '适用范围',
    exchange_method VARCHAR(32) COMMENT '兑换方式: AUTO_BIND/CODE/QR_CODE/MANUAL',
    stock_total INT DEFAULT 0 COMMENT '总库存',
    stock_used INT DEFAULT 0 COMMENT '已兑换数量',
    stock_daily_limit INT COMMENT '每日限兑',
    stock_per_user INT COMMENT '每人限兑',
    supplier_name VARCHAR(128) COMMENT '供应商名称',
    supplier_contact VARCHAR(64) COMMENT '供应商联系方式',
    refund_policy VARCHAR(32) COMMENT '退款政策: NO_REFUND/CONDITIONAL/FULL_REFUND',
    image_url VARCHAR(512) COMMENT '封面图片',
    detail_desc MEDIUMTEXT COMMENT '详细说明(富文本)',
    benefit_description VARCHAR(2000) COMMENT '权益描述',
    ai_tag VARCHAR(64) COMMENT 'AI卖点标签',
    ai_selling_point VARCHAR(2000) COMMENT 'AI卖点描述',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/ON_SHELF/OFF_SHELF/REJECTED',
    auditor VARCHAR(64) COMMENT '审核人',
    audit_time DATETIME COMMENT '审核时间',
    reject_reason VARCHAR(500) COMMENT '驳回原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_benefit_code (benefit_code)
);

-- ========== 商户审核日志表 ==========
CREATE TABLE IF NOT EXISTS merchant_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    audit_node VARCHAR(50) COMMENT '审核节点：QUALIFICATION/BUSINESS/COMPLIANCE/CONTRACT/PAYMENT',
    action VARCHAR(20) NOT NULL COMMENT 'APPROVED/REJECTED',
    operator VARCHAR(100) COMMENT '操作人',
    comment VARCHAR(500) COMMENT '审核说明',
    reject_reason VARCHAR(500) COMMENT '驳回原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant_id (merchant_id)
);

-- ========== 13. RBAC角色表 ==========
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    role_desc VARCHAR(256),
    data_scope VARCHAR(32) DEFAULT 'ALL',
    sort_order INT DEFAULT 0,
    status VARCHAR(16) DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code)
);

-- ========== 14. RBAC菜单表 ==========
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(64) NOT NULL,
    menu_type VARCHAR(16) NOT NULL DEFAULT 'MENU',
    path VARCHAR(256),
    component VARCHAR(256),
    permission VARCHAR(128),
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    visible INT DEFAULT 1,
    keep_alive INT DEFAULT 0,
    status VARCHAR(16) DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ========== 15. RBAC用户角色关联表 ==========
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ========== 16. RBAC角色菜单关联表 ==========
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ========== 17. SSO接入平台表 ==========
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

-- ========== 18. 保证金表 ==========
CREATE TABLE IF NOT EXISTS deposit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deposit_code VARCHAR(32) NOT NULL COMMENT '保证金编号',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    deposit_type VARCHAR(32) NOT NULL COMMENT '类型: PAY(缴纳)/REFUND(退还)/DEDUCT(扣除)',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    balance DECIMAL(12,2) NOT NULL COMMENT '余额',
    pay_method VARCHAR(32) COMMENT '支付方式',
    pay_no VARCHAR(64) COMMENT '支付流水号',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/COMPLETED/REJECTED',
    reason VARCHAR(500) COMMENT '原因说明',
    approver VARCHAR(64) COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_deposit_code (deposit_code),
    INDEX idx_merchant_id (merchant_id)
);

-- ========== 19. 合同模板表 ==========
CREATE TABLE IF NOT EXISTS contract_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(32) NOT NULL COMMENT '模板编号',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(32) NOT NULL COMMENT '类型: SETTLEMENT(入驻合同)/COOPERATION(合作协议)/SUPPLEMENT(补充协议)',
    content MEDIUMTEXT COMMENT '合同内容(富文本)',
    variables VARCHAR(2000) COMMENT '变量定义JSON',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_template_code (template_code)
);

-- ========== 20. 合同表 ==========
CREATE TABLE IF NOT EXISTS contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_code VARCHAR(32) NOT NULL COMMENT '合同编号',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    template_id BIGINT COMMENT '模板ID',
    contract_type VARCHAR(32) NOT NULL COMMENT '类型',
    contract_title VARCHAR(256) COMMENT '合同标题',
    contract_content MEDIUMTEXT COMMENT '合同内容',
    file_url VARCHAR(512) COMMENT '合同文件URL',
    sign_url VARCHAR(512) COMMENT '电子签章URL',
    commission_rate DECIMAL(5,4) COMMENT '佣金费率',
    deposit_amount DECIMAL(12,2) COMMENT '保证金金额',
    platform_signed INT DEFAULT 0 COMMENT '平台签署状态',
    platform_sign_time DATETIME COMMENT '平台签署时间',
    platform_signer VARCHAR(64) COMMENT '平台签署人',
    merchant_signed INT DEFAULT 0 COMMENT '商户签署状态',
    merchant_sign_time DATETIME COMMENT '商户签署时间',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PENDING_SIGN/SIGNED/TERMINATED/EXPIRED',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '到期日期',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_code (contract_code),
    INDEX idx_merchant_id (merchant_id)
);

-- ========== 21. 佣金费率配置表 ==========
CREATE TABLE IF NOT EXISTS commission_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    category_id BIGINT COMMENT '品类ID，NULL表示全品类',
    rate_type VARCHAR(32) NOT NULL DEFAULT 'FIXED' COMMENT '类型: FIXED(固定)/LADDER(阶梯)/CATEGORY(品类差异化)',
    commission_rate DECIMAL(5,4) NOT NULL COMMENT '佣金费率',
    ladder_config VARCHAR(2000) COMMENT '阶梯配置JSON',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '到期日期',
    status VARCHAR(16) DEFAULT 'ACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant_id (merchant_id)
);

-- ========== 22. 招商线索表 ==========
CREATE TABLE IF NOT EXISTS crm_lead (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_code VARCHAR(32) NOT NULL COMMENT '线索编号',
    company_name VARCHAR(128) NOT NULL COMMENT '企业名称',
    brand_name VARCHAR(128) COMMENT '品牌名称',
    industry VARCHAR(64) COMMENT '行业',
    contact_name VARCHAR(64) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(128) COMMENT '联系邮箱',
    source VARCHAR(32) COMMENT '来源: ACTIVE_MINING/REFERRAL/EXHIBITION/ONLINE/OTHER',
    status VARCHAR(32) NOT NULL DEFAULT 'NEW' COMMENT '状态: NEW/CONTACTING/NEGOTIATING/INTENT_CONFIRMED/CONVERTED/LOST',
    intention_level VARCHAR(16) COMMENT '意向等级: HIGH/MEDIUM/LOW',
    estimated_gmv DECIMAL(12,2) COMMENT '预估GMV',
    assigned_to VARCHAR(64) COMMENT '负责人',
    lost_reason VARCHAR(500) COMMENT '丢失原因',
    remark VARCHAR(2000) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_lead_code (lead_code),
    INDEX idx_status (status)
);

-- ========== 23. 招商跟进记录表 ==========
CREATE TABLE IF NOT EXISTS crm_follow_up (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_id BIGINT NOT NULL COMMENT '线索ID',
    follow_type VARCHAR(32) NOT NULL COMMENT '类型: PHONE/MEETING/EMAIL/WECHAT/VISIT',
    content VARCHAR(2000) NOT NULL COMMENT '跟进内容',
    next_plan VARCHAR(2000) COMMENT '下一步计划',
    next_follow_time DATETIME COMMENT '下次跟进时间',
    follow_by VARCHAR(64) COMMENT '跟进人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_lead_id (lead_id)
);
 
-- ========== 风控稽核相关表 ==========
CREATE TABLE IF NOT EXISTS blacklist_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(256) NOT NULL COMMENT '黑名单值',
    type VARCHAR(32) NOT NULL COMMENT '黑名单类型',
    list_type VARCHAR(32) NOT NULL COMMENT '列表类型',
    reason VARCHAR(500) COMMENT '原因',
    source VARCHAR(64) COMMENT '来源',
    operator VARCHAR(64) COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    expire_time DATETIME COMMENT '过期时间'
);
 
CREATE TABLE IF NOT EXISTS risk_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(32) NOT NULL COMMENT '事件类型',
    target VARCHAR(128) COMMENT '目标',
    risk_level VARCHAR(16) COMMENT '风险等级',
    score INT DEFAULT 0 COMMENT '风险评分',
    hit_rule VARCHAR(128) COMMENT '命中规则',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '处理状态',
    source VARCHAR(64) COMMENT '来源',
    detail TEXT COMMENT '详情',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS risk_check_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL COMMENT '规则名称',
    type VARCHAR(32) NOT NULL COMMENT '规则类型',
    scene VARCHAR(64) COMMENT '应用场景',
    priority INT DEFAULT 5 COMMENT '优先级',
    rule_condition VARCHAR(2000) COMMENT '规则条件',
    action VARCHAR(32) DEFAULT 'MANUAL' COMMENT '处置动作',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    active INT DEFAULT 1 COMMENT '是否启用',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS disposal_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL COMMENT '方案名称',
    type VARCHAR(32) NOT NULL COMMENT '处置类型',
    trigger_rule VARCHAR(128) COMMENT '触发规则',
    risk_level VARCHAR(16) COMMENT '风险等级',
    duration VARCHAR(64) COMMENT '持续时间',
    status VARCHAR(16) DEFAULT 'ACTIVE' COMMENT '状态',
    exec_count INT DEFAULT 0 COMMENT '执行次数',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS risk_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_code VARCHAR(32) COMMENT '告警编号',
    rule_id BIGINT COMMENT '规则ID',
    rule_code VARCHAR(64) COMMENT '规则编码',
    rule_name VARCHAR(128) COMMENT '规则名称',
    target_type VARCHAR(32) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    target_code VARCHAR(64) COMMENT '目标编码',
    risk_level VARCHAR(16) COMMENT '风险等级',
    alert_action VARCHAR(32) COMMENT '告警动作',
    status VARCHAR(16) DEFAULT 'PENDING' COMMENT '处理状态',
    alert_time DATETIME COMMENT '告警时间',
    handler VARCHAR(64) COMMENT '处理人',
    handle_time DATETIME COMMENT '处理时间',
    handle_result VARCHAR(256) COMMENT '处理结果',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
 
-- ========== 种子数据 ==========
INSERT INTO sys_user (username, password, real_name, phone, role, platforms, status) VALUES
('admin', '$2a$10$CC.jqflKo7lPLxy8.OsQbOLLnB3zFitqRzm1ElqtziX6WM69u7ILK', '系统管理员', '13800138000', 'SUPER_ADMIN', '1,2,3,4,5,6,7', 'ACTIVE'),
('operator01', '$2a$10$CmtZ.4HAkEJ.DoNZasHS9e5LEXDdiabOUjEXDoIzeqtuTVGKF3Rfe', '运营专员', '13800138001', 'OPERATOR', '1,2,3', 'ACTIVE'),
('auditor01', '$2a$10$CmtZ.4HAkEJ.DoNZasHS9e5LEXDdiabOUjEXDoIzeqtuTVGKF3Rfe', '稽核专员', '13800138002', 'AUDITOR', '1,3,5', 'ACTIVE');

INSERT INTO merchant (merchant_code, merchant_name, merchant_type, credit_code, legal_person, contact_name, contact_phone, onboarding_step, onboarding_status, risk_level, commission_rate) VALUES
('M20240823001', '瑞幸咖啡（中国）有限公司', 'DIGITAL', '91110108MA01ABC23X', '郭谨一', '张经理', '13900001111', 3, 'REVIEWING', 'LOW', 0.0500),
('M20240823003', '腾讯音乐', 'DIGITAL', '91440300MA1FABC67', '马化腾', '王总监', '13900003333', 8, 'APPROVED', 'LOW', 0.0300);

INSERT INTO product (product_code, product_name, category, brand, merchant_id, price, market_price, stock, sales_count, avg_score, ai_selling_point, status) VALUES
('PRD20240823001', '腾讯视频VIP会员月卡', '视频娱乐', '腾讯视频', 2, 19.90, 30.00, 1000, 568, 4.8, '🔥 腾讯视频VIP会员月卡，低至6.6折！', 'ON_SHELF'),
('PRD20240823002', '爱奇艺黄金会员月卡', '视频娱乐', '爱奇艺', 2, 22.00, 30.00, 800, 432, 4.7, '🎬 爱奇艺黄金会员月卡，院线大片抢先看！', 'ON_SHELF'),
('PRD20240823003', '瑞幸咖啡29元通兑券', '本地生活', '瑞幸咖啡', 1, 19.90, 29.00, 5000, 1256, 4.9, '☕ 瑞幸咖啡超值通兑券！', 'ON_SHELF');

INSERT INTO mall_order (order_code, merchant_id, product_id, customer_phone, quantity, order_amount, ai_dou_deduct, pay_amount, status) VALUES
('ORD202408230001', 2, 1, '13812345678', 1, 19.90, 2.00, 17.90, 'EVALUATED'),
('ORD202408230002', 1, 3, '13812345679', 2, 39.80, 5.00, 34.80, 'PAID');

INSERT INTO evaluation (order_id, merchant_id, product_id, user_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status) VALUES
(1, 2, 1, '13812345678', 5, 5, 5, 5, 5, '非常好用！开通秒到账，没有广告太爽了！', '快速到账,无广告,画质清晰', 'POSITIVE', 'AUTO_PASS');

INSERT INTO settlement (settle_code, merchant_id, settle_type, settle_period, total_amount, item_count, status) VALUES
('STL20240823001', 2, 'COMMISSION', '2024年8月', 85600.00, 1256, 'COMPLETED'),
('STL20240823002', 1, 'AI_DOU', '2024年8月', 32000.00, 56, 'PENDING');

INSERT INTO audit_record (audit_code, audit_type, target_code, merchant_id, risk_type, risk_level, amount, description, status) VALUES
('AUD20240823001', 'ORDER', 'ORD202408230002', 1, '刷单嫌疑', 'HIGH', 34.80, '同一IP在10分钟内下单15笔', 'PENDING'),
('AUD20240823002', 'FUND', 'STL20240823002', 1, '金额波动', 'MEDIUM', 32000.00, '结算金额较上月增长215%', 'CHECKING');

INSERT INTO api_config (api_name, api_path, target_system, auth_type, rate_limit) VALUES
('4A统一认证', '/api/auth/sso', '4A_SYSTEM', 'JWT', 200),
('OCR资质识别', '/api/ai/ocr', 'AI_PLATFORM', 'JWT', 50);

INSERT INTO sso_platform (id, name, system_code, auth_type, icon, url, status) VALUES
(1, '生态合作平台', 'ECO_PLATFORM', 'OAUTH', '🏢', NULL, 'ACTIVE'),
(2, '积分商城后台', 'POINTS_MALL', 'JWT', '🎁', NULL, 'ACTIVE'),
(3, '权益超市后台', 'BENEFIT_MART', 'JWT', '🎬', NULL, 'ACTIVE'),
(4, '泛全联盟平台', 'ALLIANCE', 'OAUTH', '🌐', NULL, 'ACTIVE'),
(5, '风控稽核管理平台', 'RISK_AUDIT', 'JWT', '🛡️', '/risk', 'ACTIVE'),
(6, '工单管理系统', 'WORK_ORDER', 'JWT', '🎫', NULL, 'ACTIVE'),
(7, '评价管理系统', 'EVALUATION', 'JWT', '⭐', NULL, 'ACTIVE'),
(8, 'C端商城', 'C_MALL', 'JWT', '🛒', '/mall', 'ACTIVE');
