-- ================================================================
-- 商城生态运营系统 MySQL 数据库设计
-- 版本：v1.0
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci
-- 设计规范：第三范式（3NF）
-- ================================================================

CREATE DATABASE IF NOT EXISTS igou_mall 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE igou_mall;

-- ================================================================
-- 1. 系统用户表（B端管理员）
-- ================================================================
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

-- ================================================================
-- 2. C端客户表
-- ================================================================
CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(128) COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(512) COMMENT '头像URL',
    email VARCHAR(128) COMMENT '邮箱',
    vip_level VARCHAR(16) DEFAULT 'NORMAL' COMMENT 'VIP等级：NORMAL/GOLD/PLATINUM/DIAMOND',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计消费金额',
    order_count INT DEFAULT 0 COMMENT '累计订单数',
    birth_date DATE COMMENT '生日',
    gender VARCHAR(8) COMMENT '性别：MALE/FEMALE/UNKNOWN',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/FROZEN/DELETED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_phone (phone),
    KEY idx_vip_level (vip_level),
    KEY idx_status (status),
    KEY idx_register_time (register_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端客户表';

-- ================================================================
-- 3. 客户标签表（客户画像标签）
-- ================================================================
CREATE TABLE customer_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    tag_name VARCHAR(64) NOT NULL COMMENT '标签名称',
    tag_category VARCHAR(64) COMMENT '标签分类：CONSUMPTION/BEHAVIOR/INTEREST/DEMOGRAPHIC',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_customer_id (customer_id),
    KEY idx_tag_category (tag_category),
    KEY idx_customer_tag (customer_id, tag_name),
    CONSTRAINT fk_customer_tag_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签表';

-- ================================================================
-- 4. 客户行为记录表
-- ================================================================
CREATE TABLE customer_behavior (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    behavior_type VARCHAR(32) NOT NULL COMMENT '行为类型：VIEW/PURCHASE/COLLECT/CART/SEARCH/SHARE',
    target_type VARCHAR(32) COMMENT '目标类型：PRODUCT/CATEGORY/MERCHANT',
    target_id BIGINT COMMENT '目标ID',
    behavior_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    device_type VARCHAR(32) COMMENT '设备类型：PC/MOBILE/TABLET',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_customer_id (customer_id),
    KEY idx_behavior_type (behavior_type),
    KEY idx_behavior_time (behavior_time),
    KEY idx_customer_behavior (customer_id, behavior_type, behavior_time),
    CONSTRAINT fk_behavior_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户行为记录表';

-- ================================================================
-- 5. 商户表
-- ================================================================
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
    province VARCHAR(64) COMMENT '省份',
    city VARCHAR(64) COMMENT '城市',
    district VARCHAR(64) COMMENT '区县',
    address VARCHAR(512) COMMENT '详细地址',
    bank_name VARCHAR(128) COMMENT '开户银行',
    bank_account VARCHAR(64) COMMENT '银行账号',
    tax_number VARCHAR(64) COMMENT '纳税人识别号',
    onboarding_step INT NOT NULL DEFAULT 1 COMMENT '入驻当前环节（1-8）',
    onboarding_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '入驻状态：PENDING/REVIEWING/APPROVED/REJECTED',
    risk_level VARCHAR(16) DEFAULT 'LOW' COMMENT '风险等级：LOW/MEDIUM/HIGH',
    merchant_grade VARCHAR(8) DEFAULT 'B' COMMENT '商户等级：A/B/C/D',
    commission_rate DECIMAL(5,4) DEFAULT 0.0500 COMMENT '佣金比例',
    settle_account VARCHAR(64) COMMENT '结算账户',
    settle_cycle VARCHAR(32) DEFAULT 'WEEKLY' COMMENT '结算周期：DAILY/WEEKLY/MONTHLY',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/FROZEN/SUSPENDED/DELETED',
    reject_reason TEXT COMMENT '驳回原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_merchant_code (merchant_code),
    KEY idx_type (merchant_type),
    KEY idx_status (status),
    KEY idx_onboarding_status (onboarding_status),
    KEY idx_risk (risk_level),
    KEY idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户表';

-- ================================================================
-- 6. 商户资质表
-- ================================================================
CREATE TABLE merchant_qualification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    qual_type VARCHAR(64) NOT NULL COMMENT '资质类型：BUSINESS_LICENSE/TAX_CERTIFICATE/LEGAL_PERSON_ID/OTHER',
    qual_name VARCHAR(128) NOT NULL COMMENT '资质名称',
    qual_file_url VARCHAR(512) NOT NULL COMMENT '资质文件URL',
    audit_status VARCHAR(32) DEFAULT 'PENDING' COMMENT '审核状态：PENDING/PASSED/REJECTED',
    audit_time DATETIME COMMENT '审核时间',
    auditor VARCHAR(64) COMMENT '审核人',
    audit_comment VARCHAR(512) COMMENT '审核意见',
    expire_date DATE COMMENT '有效期至',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_merchant_id (merchant_id),
    KEY idx_qual_type (qual_type),
    KEY idx_audit_status (audit_status),
    CONSTRAINT fk_qualification_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户资质表';

-- ================================================================
-- 7. 商品分类表
-- ================================================================
CREATE TABLE product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_code VARCHAR(32) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    level INT NOT NULL DEFAULT 1 COMMENT '分类级别：1/2/3',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    icon_url VARCHAR(512) COMMENT '图标URL',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_id (parent_id),
    KEY idx_level (level),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ================================================================
-- 8. 商品表
-- ================================================================
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_code VARCHAR(32) NOT NULL COMMENT '商品编号',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    category VARCHAR(64) NOT NULL COMMENT '品类名称',
    brand VARCHAR(64) COMMENT '品牌',
    merchant_id BIGINT NOT NULL COMMENT '所属商户ID',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    market_price DECIMAL(10,2) COMMENT '市场价',
    vip_price DECIMAL(10,2) COMMENT 'VIP价',
    stock INT DEFAULT 0 COMMENT '库存',
    sales_count INT DEFAULT 0 COMMENT '销量',
    avg_score DECIMAL(3,1) DEFAULT 0.0 COMMENT '平均评分',
    description TEXT COMMENT '商品描述',
    image_urls VARCHAR(2048) COMMENT '商品图片URL（逗号分隔）',
    ai_selling_point TEXT COMMENT 'AI生成的卖点文案',
    ai_tag VARCHAR(64) COMMENT 'AI状态标签',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/ON_SHELF/OFF_SHELF/REJECTED',
    weight DECIMAL(8,2) COMMENT '重量（千克）',
    volume DECIMAL(10,2) COMMENT '体积（立方厘米）',
    origin VARCHAR(64) COMMENT '产地',
    spec VARCHAR(256) COMMENT '规格',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热销：0-否 1-是',
    is_new TINYINT DEFAULT 0 COMMENT '是否新品：0-否 1-是',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    audit_time DATETIME COMMENT '审核时间',
    auditor VARCHAR(64) COMMENT '审核人',
    reject_reason TEXT COMMENT '驳回原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_merchant (merchant_id),
    KEY idx_category (category_id),
    KEY idx_status (status),
    KEY idx_is_hot (is_hot),
    KEY idx_price (price),
    KEY idx_sales_count (sales_count),
    KEY idx_merchant_status (merchant_id, status),
    CONSTRAINT fk_product_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_category(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ================================================================
-- 9. 库存变动记录表
-- ================================================================
CREATE TABLE stock_change (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    change_type VARCHAR(32) NOT NULL COMMENT '变动类型：IN/OUT/ADJUST',
    change_amount INT NOT NULL COMMENT '变动数量',
    before_stock INT NOT NULL COMMENT '变动前库存',
    after_stock INT NOT NULL COMMENT '变动后库存',
    order_code VARCHAR(32) COMMENT '关联订单编号',
    operator VARCHAR(64) COMMENT '操作人',
    reason VARCHAR(256) COMMENT '变动原因',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_product_id (product_id),
    KEY idx_change_type (change_type),
    KEY idx_create_time (create_time),
    CONSTRAINT fk_stock_change_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变动记录表';

-- ================================================================
-- 10. 购物车表
-- ================================================================
CREATE TABLE shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中：0-否 1-是',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_cart_customer_product (customer_id, product_id),
    KEY idx_customer_id (customer_id),
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ================================================================
-- 11. 收货地址表
-- ================================================================
CREATE TABLE delivery_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
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
    KEY idx_customer_id (customer_id),
    KEY idx_is_default (is_default),
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ================================================================
-- 12. 订单表
-- ================================================================
CREATE TABLE mall_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    customer_id BIGINT COMMENT '客户ID',
    customer_phone VARCHAR(20) COMMENT '客户手机号',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(512) COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    order_amount DECIMAL(12,2) NOT NULL COMMENT '订单金额',
    ai_dou_deduct DECIMAL(12,2) DEFAULT 0.00 COMMENT 'AI豆抵扣金额',
    pay_amount DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    discount_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT '状态：CREATED/PAID/FULFILLED/EVALUATED/REFUNDED/CANCELLED',
    pay_method VARCHAR(32) COMMENT '支付方式：ALIPAY/WECHAT/BANK/CASH',
    pay_time DATETIME COMMENT '支付时间',
    pay_no VARCHAR(64) COMMENT '支付流水号',
    delivery_address_id BIGINT COMMENT '收货地址ID',
    logistics_no VARCHAR(64) COMMENT '物流单号',
    logistics_company VARCHAR(64) COMMENT '物流公司',
    fulfill_time DATETIME COMMENT '履约时间',
    refund_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',
    refund_time DATETIME COMMENT '退款时间',
    refund_reason TEXT COMMENT '退款原因',
    cancel_reason TEXT COMMENT '取消原因',
    cancel_time DATETIME COMMENT '取消时间',
    remark VARCHAR(512) COMMENT '订单备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_code (order_code),
    KEY idx_merchant (merchant_id),
    KEY idx_status (status),
    KEY idx_customer (customer_id),
    KEY idx_create_time (create_time),
    KEY idx_pay_time (pay_time),
    KEY idx_merchant_status (merchant_id, status),
    KEY idx_customer_status (customer_id, status),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL,
    CONSTRAINT fk_order_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ================================================================
-- 13. 退款申请表
-- ================================================================
CREATE TABLE refund_apply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    refund_type VARCHAR(32) NOT NULL COMMENT '退款类型：REFUND_ONLY/REFUND_WITH_RETURN',
    refund_amount DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(512) COMMENT '退款原因',
    images VARCHAR(2048) COMMENT '凭证图片（逗号分隔）',
    status VARCHAR(32) NOT NULL DEFAULT 'APPLIED' COMMENT '状态：APPLIED/REVIEWING/APPROVED/REJECTED/REFUNDED',
    merchant_remark VARCHAR(512) COMMENT '商户备注',
    audit_time DATETIME COMMENT '审核时间',
    auditor VARCHAR(64) COMMENT '审核人',
    refund_time DATETIME COMMENT '退款时间',
    refund_no VARCHAR(64) COMMENT '退款流水号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_order_code (order_code),
    KEY idx_status (status),
    KEY idx_customer_id (customer_id),
    KEY idx_merchant_id (merchant_id),
    CONSTRAINT fk_refund_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_refund_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表';

-- ================================================================
-- 14. 结算表
-- ================================================================
CREATE TABLE settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    settle_code VARCHAR(32) NOT NULL COMMENT '结算编号',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    settle_type VARCHAR(32) NOT NULL COMMENT '结算类型：AI_DOU/COMMISSION/EXPANSION',
    settle_period VARCHAR(32) NOT NULL COMMENT '结算周期',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '结算总金额',
    commission_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '佣金金额',
    ai_dou_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT 'AI豆金额',
    item_count INT NOT NULL DEFAULT 0 COMMENT '结算笔数',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/CHECKING/APPROVED/COMPLETED/FAILED',
    approver VARCHAR(64) COMMENT '审批人',
    approve_time DATETIME COMMENT '审批时间',
    pay_time DATETIME COMMENT '打款时间',
    bank_flow_no VARCHAR(64) COMMENT '银行流水号',
    remark VARCHAR(512) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_settle_code (settle_code),
    KEY idx_merchant (merchant_id),
    KEY idx_type (settle_type),
    KEY idx_status (status),
    KEY idx_period (settle_period),
    CONSTRAINT fk_settle_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算表';

-- ================================================================
-- 15. 结算明细表
-- ================================================================
CREATE TABLE settlement_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    settle_id BIGINT NOT NULL COMMENT '结算ID',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    order_amount DECIMAL(12,2) NOT NULL COMMENT '订单金额',
    commission_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '佣金比例',
    commission_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '佣金金额',
    ai_dou_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT 'AI豆金额',
    merchant_amount DECIMAL(12,2) NOT NULL COMMENT '商户应收金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_settle_id (settle_id),
    KEY idx_order_code (order_code),
    CONSTRAINT fk_settle_detail_settle FOREIGN KEY (settle_id) REFERENCES settlement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结算明细表';

-- ================================================================
-- 16. 发票表
-- ================================================================
CREATE TABLE invoice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    invoice_code VARCHAR(64) COMMENT '发票代码',
    invoice_no VARCHAR(64) COMMENT '发票号码',
    order_code VARCHAR(32) NOT NULL COMMENT '订单编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    invoice_type VARCHAR(32) NOT NULL DEFAULT 'PERSONAL' COMMENT '发票类型：PERSONAL/COMPANY/ELECTRONIC',
    title VARCHAR(256) NOT NULL COMMENT '发票抬头',
    tax_number VARCHAR(64) COMMENT '纳税人识别号',
    amount DECIMAL(12,2) NOT NULL COMMENT '发票金额',
    status VARCHAR(32) NOT NULL DEFAULT 'APPLIED' COMMENT '状态：APPLIED/PROCESSING/ISSUED/FAILED',
    issue_time DATETIME COMMENT '开具时间',
    pdf_url VARCHAR(512) COMMENT '发票PDF URL',
    remark VARCHAR(512) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_order_code (order_code),
    KEY idx_customer_id (customer_id),
    KEY idx_status (status),
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- ================================================================
-- 17. 对账记录表
-- ================================================================
CREATE TABLE reconciliation_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
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
    UNIQUE KEY uk_recon_code (recon_code),
    KEY idx_period (recon_period),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账记录表';

-- ================================================================
-- 18. 风控规则表
-- ================================================================
CREATE TABLE risk_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_code VARCHAR(32) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(32) NOT NULL COMMENT '规则类型：ORDER/USER/MERCHANT/PAYMENT',
    rule_condition TEXT NOT NULL COMMENT '规则条件（JSON）',
    rule_action VARCHAR(32) NOT NULL COMMENT '触发动作：ALERT/BLOCK/FREEZE/REVIEW',
    priority INT DEFAULT 100 COMMENT '优先级（数值越小优先级越高）',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE',
    description TEXT COMMENT '规则描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_rule_code (rule_code),
    KEY idx_rule_type (rule_type),
    KEY idx_status (status),
    KEY idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控规则表';

-- ================================================================
-- 19. 风控告警表
-- ================================================================
CREATE TABLE risk_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    alert_code VARCHAR(32) NOT NULL COMMENT '告警编号',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_code VARCHAR(32) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    target_type VARCHAR(32) NOT NULL COMMENT '目标类型：ORDER/USER/MERCHANT',
    target_id BIGINT COMMENT '目标ID',
    target_code VARCHAR(32) COMMENT '目标编号',
    risk_level VARCHAR(16) NOT NULL DEFAULT 'LOW' COMMENT '风险等级：LOW/MEDIUM/HIGH/CRITICAL',
    alert_action VARCHAR(32) NOT NULL COMMENT '告警动作：ALERT/BLOCK/FREEZE/REVIEW',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/RESOLVED/IGNORED',
    alert_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '告警时间',
    handler VARCHAR(64) COMMENT '处理人',
    handle_time DATETIME COMMENT '处理时间',
    handle_result TEXT COMMENT '处理结果',
    remark VARCHAR(512) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_alert_code (alert_code),
    KEY idx_rule_id (rule_id),
    KEY idx_target_type (target_type),
    KEY idx_risk_level (risk_level),
    KEY idx_status (status),
    KEY idx_alert_time (alert_time),
    CONSTRAINT fk_alert_rule FOREIGN KEY (rule_id) REFERENCES risk_rule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控告警表';

-- ================================================================
-- 20. 交易监控表
-- ================================================================
CREATE TABLE transaction_monitor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    monitor_code VARCHAR(32) NOT NULL COMMENT '监控编号',
    monitor_type VARCHAR(32) NOT NULL COMMENT '监控类型：REAL_TIME/DAILY/HOURLY',
    metric_type VARCHAR(64) NOT NULL COMMENT '指标类型：ORDER_COUNT/ORDER_AMOUNT/PAY_SUCCESS_RATE/REFUND_RATE',
    threshold_value DECIMAL(12,2) COMMENT '阈值',
    current_value DECIMAL(12,2) COMMENT '当前值',
    is_breach TINYINT DEFAULT 0 COMMENT '是否超限：0-否 1-是',
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL/WARNING/CRITICAL',
    monitor_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '监控时间',
    remark VARCHAR(512) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_monitor_code (monitor_code),
    KEY idx_monitor_type (monitor_type),
    KEY idx_metric_type (metric_type),
    KEY idx_is_breach (is_breach),
    KEY idx_monitor_time (monitor_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易监控表';

-- ================================================================
-- 21. 评价表
-- ================================================================
CREATE TABLE evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_phone VARCHAR(20) COMMENT '客户手机号',
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
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_order (order_id),
    KEY idx_merchant (merchant_id),
    KEY idx_product (product_id),
    KEY idx_customer (customer_id),
    KEY idx_sentiment (sentiment),
    CONSTRAINT fk_eval_order FOREIGN KEY (order_id) REFERENCES mall_order(id) ON DELETE CASCADE,
    CONSTRAINT fk_eval_merchant FOREIGN KEY (merchant_id) REFERENCES merchant(id) ON DELETE CASCADE,
    CONSTRAINT fk_eval_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT fk_eval_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ================================================================
-- 22. AI任务表
-- ================================================================
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
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_type (task_type),
    KEY idx_biz (biz_type, biz_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI任务表';

-- ================================================================
-- 23. 接口配置表
-- ================================================================
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
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_api_path (api_path),
    KEY idx_target (target_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口配置表';

-- ================================================================
-- 24. 系统日志表
-- ================================================================
CREATE TABLE sys_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    log_type VARCHAR(32) NOT NULL COMMENT '日志类型：OPERATION/AUDIT/ERROR/ACCESS',
    operator VARCHAR(64) COMMENT '操作人',
    operator_id BIGINT COMMENT '操作人ID',
    operation VARCHAR(128) COMMENT '操作描述',
    module VARCHAR(64) COMMENT '模块名称',
    target_type VARCHAR(32) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    detail TEXT COMMENT '操作详情（JSON）',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent VARCHAR(512) COMMENT '用户代理',
    result VARCHAR(16) DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAILED',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_log_type (log_type),
    KEY idx_operator (operator),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- ================================================================
-- 种子数据
-- ================================================================
INSERT INTO sys_user (username, password, real_name, phone, role, platforms, status) VALUES
('admin', '$2a$10$CC.jqflKo7lPLxy8.OsQbOLLnB3zFitqRzm1ElqtziX6WM69u7ILK', '系统管理员', '13800138000', 'SUPER_ADMIN', '1,2,3,4,5,6,7', 'ACTIVE'),
('operator01', '$2a$10$CC.jqflKo7lPLxy8.OsQbOLLnB3zFitqRzm1ElqtziX6WM69u7ILK', '运营专员', '13800138001', 'OPERATOR', '1,2,3', 'ACTIVE'),
('auditor01', '$2a$10$CC.jqflKo7lPLxy8.OsQbOLLnB3zFitqRzm1ElqtziX6WM69u7ILK', '稽核专员', '13800138002', 'AUDITOR', '1,3,5', 'ACTIVE'),
('finance01', '$2a$10$CC.jqflKo7lPLxy8.OsQbOLLnB3zFitqRzm1ElqtziX6WM69u7ILK', '财务专员', '13800138003', 'SYS_ADMIN', '1', 'ACTIVE'),
('risk01', '$2a$10$CC.jqflKo7lPLxy8.OsQbOLLnB3zFitqRzm1ElqtziX6WM69u7ILK', '风控专员', '13800138004', 'SYS_ADMIN', '1,5', 'ACTIVE');

INSERT INTO product_category (category_code, category_name, parent_id, level, sort_order, status) VALUES
('CAT001', '视频娱乐', 0, 1, 1, 'ACTIVE'),
('CAT002', '餐饮美食', 0, 1, 2, 'ACTIVE'),
('CAT003', '生活服务', 0, 1, 3, 'ACTIVE'),
('CAT004', '数字商品', 0, 1, 4, 'ACTIVE'),
('CAT005', '旅游出行', 0, 1, 5, 'ACTIVE'),
('CAT006', '购物权益', 0, 1, 6, 'ACTIVE'),
('CAT00101', '视频会员', 1, 2, 1, 'ACTIVE'),
('CAT00102', '音乐会员', 1, 2, 2, 'ACTIVE'),
('CAT00201', '咖啡饮品', 2, 2, 1, 'ACTIVE'),
('CAT00202', '餐饮套餐', 2, 2, 2, 'ACTIVE'),
('CAT00301', '家政服务', 3, 2, 1, 'ACTIVE'),
('CAT00302', '美容美发', 3, 2, 2, 'ACTIVE'),
('CAT00401', '游戏点卡', 4, 2, 1, 'ACTIVE'),
('CAT00402', '软件激活', 4, 2, 2, 'ACTIVE'),
('CAT00501', '酒店预订', 5, 2, 1, 'ACTIVE'),
('CAT00502', '机票预订', 5, 2, 2, 'ACTIVE');

INSERT INTO merchant (merchant_code, merchant_name, merchant_type, credit_code, legal_person, contact_name, contact_phone, province, city, bank_name, bank_account, tax_number, onboarding_step, onboarding_status, risk_level, merchant_grade, commission_rate, settle_cycle, status) VALUES
('M20240823001', '瑞幸咖啡（中国）有限公司', 'LOCAL_LIFE', '91110108MA01ABC23X', '郭谨一', '张经理', '13900001111', '北京市', '北京市', '中国工商银行', '6222021234567890', '91110108MA01ABC23X', 8, 'APPROVED', 'LOW', 'A', 0.0500, 'WEEKLY', 'ACTIVE'),
('M20240823002', '上海寻梦信息技术有限公司', 'PHYSICAL', '91310000MA1FABC45', '陈磊', '李总', '13900002222', '上海市', '上海市', '中国建设银行', '6227001234567890', '91310000MA1FABC45', 8, 'APPROVED', 'LOW', 'A', 0.0800, 'MONTHLY', 'ACTIVE'),
('M20240823003', '深圳腾讯计算机系统有限公司', 'DIGITAL', '91440300MA1FABC67', '马化腾', '王总监', '13900003333', '广东省', '深圳市', '招商银行', '6225881234567890', '91440300MA1FABC67', 8, 'APPROVED', 'LOW', 'A', 0.0300, 'WEEKLY', 'ACTIVE'),
('M20240823004', '阿里巴巴（中国）有限公司', 'PHYSICAL', '91330100MA1FABC89', '张勇', '赵经理', '13900004444', '浙江省', '杭州市', '中国农业银行', '6228481234567890', '91330100MA1FABC89', 8, 'APPROVED', 'MEDIUM', 'A', 0.0600, 'MONTHLY', 'ACTIVE'),
('M20240823005', '爱奇艺（北京）科技有限公司', 'DIGITAL', '91110108MA01ABC12', '龚宇', '刘经理', '13900005555', '北京市', '北京市', '中国工商银行', '6222021234567891', '91110108MA01ABC12', 8, 'APPROVED', 'LOW', 'B', 0.0400, 'WEEKLY', 'ACTIVE'),
('M20240823006', '优酷信息技术（北京）有限公司', 'DIGITAL', '91110108MA01ABC34', '樊路远', '孙经理', '13900006666', '北京市', '北京市', '中国建设银行', '6227001234567891', '91110108MA01ABC34', 8, 'APPROVED', 'LOW', 'B', 0.0400, 'WEEKLY', 'ACTIVE');

INSERT INTO product (product_code, product_name, category_id, category, brand, merchant_id, price, market_price, vip_price, stock, sales_count, avg_score, description, image_urls, ai_selling_point, status, is_hot, is_new, is_recommend, sort_order) VALUES
('PRD20240823001', '腾讯视频VIP会员月卡', 7, '视频会员', '腾讯视频', 3, 19.90, 30.00, 17.90, 10000, 12568, 4.8, '腾讯视频VIP会员月卡，畅享海量影视内容，无广告观看，支持多设备同时登录。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tencent%20video%20vip%20card%20premium%20membership&image_size=square', '🔥 腾讯视频VIP月卡，低至6.6折！追剧不等待，大片随心看！4K超清+无广告+多设备同登，一站式满足全家观影需求。', 'ON_SHELF', 1, 0, 1, 1),
('PRD20240823002', '爱奇艺黄金会员月卡', 7, '视频会员', '爱奇艺', 5, 22.00, 30.00, 19.80, 8000, 8934, 4.7, '爱奇艺黄金VIP会员月卡，抢先看院线大片，畅享高清画质。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=iqiyi%20gold%20vip%20card%20premium%20video&image_size=square', '🎬 爱奇艺黄金VIP月卡，院线大片抢先看！4K超清画质，沉浸式观影体验，纯净无广告！', 'ON_SHELF', 1, 0, 1, 2),
('PRD20240823003', '瑞幸咖啡29元饮品通兑券', 9, '咖啡饮品', '瑞幸咖啡', 1, 19.90, 29.00, 18.90, 50000, 35678, 4.9, '瑞幸咖啡29元饮品通兑券，全场饮品通用，门店自提。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luckin%20coffee%20voucher%20coupon%20drink&image_size=square', '☕ 瑞幸咖啡超值通兑券！全场饮品通用，新鲜现做，门店自提，咖啡自由即刻拥有！', 'ON_SHELF', 1, 0, 1, 3),
('PRD20240823004', '优酷VIP会员季卡', 7, '视频会员', '优酷', 6, 45.00, 68.00, 40.50, 6000, 5423, 4.6, '优酷VIP会员季卡，热门综艺、独播大剧一网打尽。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=youku%20vip%20card%20premium%20video&image_size=square', '📺 优酷VIP季卡，热门综艺看不停！独播大剧、院线大片、品质综艺一站式享！', 'ON_SHELF', 0, 0, 1, 4),
('PRD20240823005', '芒果TV会员月卡', 7, '视频会员', '芒果TV', 3, 18.80, 25.00, 16.90, 7500, 6789, 4.5, '芒果TV会员月卡，热门综艺随心看，《乘风破浪》《向往的生活》独家追！', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=mango%20tv%20vip%20card%20premium%20entertainment&image_size=square', '🥭 芒果TV会员月卡，热门综艺随心看！《乘风破浪》《向往的生活》等热门综艺独家追！', 'ON_SHELF', 0, 1, 0, 5),
('PRD20240823006', 'QQ音乐绿钻豪华版月卡', 8, '音乐会员', 'QQ音乐', 3, 15.00, 25.00, 13.50, 5000, 4321, 4.7, 'QQ音乐绿钻豪华版月卡，无损音质，海量曲库，专属音效。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=qq%20music%20green%20diamond%20vip%20card&image_size=square', '🎵 QQ音乐绿钻豪华版，无损音质畅享！千万曲库随心听，专属音效打造极致听觉体验！', 'ON_SHELF', 0, 0, 0, 6),
('PRD20240823007', '瑞幸咖啡58元双人套餐券', 10, '餐饮套餐', '瑞幸咖啡', 1, 39.90, 58.00, 37.90, 20000, 12345, 4.8, '瑞幸咖啡58元双人套餐券，包含两杯饮品及小食。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luckin%20coffee%20couple%20meal%20set%20voucher&image_size=square', '👫 瑞幸咖啡双人套餐券！精选饮品搭配美味小食，约上好友共享美好时光！', 'ON_SHELF', 1, 1, 1, 7),
('PRD20240823008', '网易云音乐黑胶VIP月卡', 8, '音乐会员', '网易云音乐', 4, 15.00, 25.00, 13.50, 6000, 5678, 4.8, '网易云音乐黑胶VIP月卡，畅享无损音质，解锁专属皮肤。', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=netease%20cloud%20music%20vip%20card%20vinyl&image_size=square', '🎶 网易云音乐黑胶VIP，海量曲库+无损音质+专属皮肤，音乐爱好者必备！', 'ON_SHELF', 0, 0, 1, 8);

INSERT INTO customer (phone, password, nickname, vip_level, total_amount, order_count, status) VALUES
('13812345678', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '购物达人', 'GOLD', 298.50, 15, 'ACTIVE'),
('13812345679', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '美食家', 'PLATINUM', 1568.00, 42, 'ACTIVE'),
('13812345680', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '追剧狂人', 'DIAMOND', 5890.00, 128, 'ACTIVE'),
('13900001111', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '新用户', 'NORMAL', 0.00, 0, 'ACTIVE');

INSERT INTO customer_tag (customer_id, tag_name, tag_category) VALUES
(1, '高频消费', 'CONSUMPTION'),
(1, '咖啡爱好者', 'INTEREST'),
(1, '移动端用户', 'BEHAVIOR'),
(2, '高价值客户', 'CONSUMPTION'),
(2, '美食达人', 'INTEREST'),
(2, '周末购物', 'BEHAVIOR'),
(3, '顶级VIP', 'CONSUMPTION'),
(3, '视频会员', 'INTEREST'),
(3, '深夜购物', 'BEHAVIOR');

INSERT INTO mall_order (order_code, customer_id, customer_phone, merchant_id, product_id, product_name, price, quantity, order_amount, ai_dou_deduct, pay_amount, status, pay_method, pay_time, create_time) VALUES
('ORD202408230001', 1, '13812345678', 3, 1, '腾讯视频VIP会员月卡', 19.90, 1, 19.90, 2.00, 17.90, 'EVALUATED', 'WECHAT', '2024-08-20 14:30:00', '2024-08-20 14:28:00'),
('ORD202408230002', 2, '13812345679', 1, 3, '瑞幸咖啡29元饮品通兑券', 19.90, 2, 39.80, 5.00, 34.80, 'FULFILLED', 'ALIPAY', '2024-08-21 10:15:00', '2024-08-21 10:10:00'),
('ORD202408230003', 3, '13812345680', 5, 2, '爱奇艺黄金会员月卡', 22.00, 1, 22.00, 0.00, 22.00, 'PAID', 'WECHAT', '2024-08-22 20:00:00', '2024-08-22 19:55:00'),
('ORD202408230004', 1, '13812345678', 1, 3, '瑞幸咖啡29元饮品通兑券', 19.90, 1, 19.90, 1.00, 18.90, 'PAID', 'ALIPAY', '2024-08-23 09:30:00', '2024-08-23 09:25:00');

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status) VALUES
(1, 3, 1, 1, '13812345678', 5, 5, 5, 5, 5, '非常好用！开通秒到账，没有广告太爽了！', '快速到账,无广告,画质清晰', 'POSITIVE', 'AUTO_PASS'),
(2, 1, 3, 2, '13812345679', 4, 5, 4, 4, 5, '咖啡很好喝，兑换也很方便', '口感好,兑换便捷', 'POSITIVE', 'AUTO_PASS');

INSERT INTO settlement (settle_code, merchant_id, settle_type, settle_period, total_amount, commission_amount, item_count, status, approver, approve_time, pay_time) VALUES
('STL20240823001', 3, 'COMMISSION', '2024-08', 8560.00, 256.80, 456, 'COMPLETED', 'finance01', '2024-08-20 10:00:00', '2024-08-20 15:00:00'),
('STL20240823002', 1, 'COMMISSION', '2024-08', 32000.00, 1600.00, 1658, 'COMPLETED', 'finance01', '2024-08-20 10:30:00', '2024-08-20 15:30:00'),
('STL20240823003', 2, 'COMMISSION', '2024-08', 12580.00, 1006.40, 234, 'APPROVED', 'finance01', '2024-08-23 09:00:00', NULL);

INSERT INTO risk_rule (rule_code, rule_name, rule_type, rule_condition, rule_action, priority, status, description) VALUES
('RULE001', '高频下单检测', 'ORDER', '{"condition": "order_count > 5", "time_window": "5m"}', 'ALERT', 10, 'ACTIVE', '同一用户5分钟内下单超过5笔触发告警'),
('RULE002', '大额订单检测', 'ORDER', '{"condition": "order_amount > 10000"}', 'REVIEW', 20, 'ACTIVE', '单笔订单金额超过10000元需人工审核'),
('RULE003', '异常IP检测', 'USER', '{"condition": "distinct_ip_count > 10", "time_window": "1h"}', 'BLOCK', 15, 'ACTIVE', '同一用户1小时内使用超过10个不同IP登录触发封禁'),
('RULE004', '商户退款率检测', 'MERCHANT', '{"condition": "refund_rate > 0.3", "time_window": "24h"}', 'ALERT', 30, 'ACTIVE', '商户24小时退款率超过30%触发告警'),
('RULE005', '连续失败支付', 'PAYMENT', '{"condition": "fail_count > 3", "time_window": "10m"}', 'FREEZE', 25, 'ACTIVE', '同一订单10分钟内支付失败超过3次冻结订单');

INSERT INTO api_config (api_name, api_path, target_system, auth_type, rate_limit) VALUES
('4A统一认证', '/api/auth/sso', '4A_SYSTEM', 'OAUTH', 200),
('OCR资质识别', '/api/ai/ocr', 'AI_PLATFORM', 'JWT', 50),
('价格采集', '/api/ai/price-collect', 'AI_PLATFORM', 'JWT', 30),
('支付收银', '/api/pay/cashier', 'BASE_CAPABILITY', 'JWT', 500),
('订单同步', '/api/order/sync', 'BASE_CAPABILITY', 'JWT', 500),
('微信登录', '/api/auth/wechat', 'BASE_CAPABILITY', 'API_KEY', 300),
('短信验证码', '/api/auth/sms', 'BASE_CAPABILITY', 'API_KEY', 100);

-- ================================================================
-- 25. SSO接入平台表
-- ================================================================
CREATE TABLE sso_platform (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(64) NOT NULL COMMENT '平台名称',
    system_code VARCHAR(64) COMMENT '系统标识',
    auth_type VARCHAR(32) DEFAULT 'OAUTH' COMMENT '认证方式：OAUTH/JWT/API_KEY',
    icon VARCHAR(16) COMMENT '图标',
    url VARCHAR(256) COMMENT '平台URL',
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/PENDING',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SSO接入平台表';

INSERT INTO sso_platform (id, name, system_code, auth_type, icon, url, status) VALUES
(1, '生态合作平台', 'ECO_PLATFORM', 'OAUTH', '🏢', NULL, 'ACTIVE'),
(2, '积分商城后台', 'POINTS_MALL', 'JWT', '🎁', NULL, 'ACTIVE'),
(3, '权益超市后台', 'BENEFIT_MART', 'JWT', '🎬', NULL, 'ACTIVE'),
(4, '泛全联盟平台', 'ALLIANCE', 'OAUTH', '🌐', NULL, 'ACTIVE'),
(5, '风控稽核管理平台', 'RISK_AUDIT', 'JWT', '🛡️', 'http://localhost:3001', 'ACTIVE'),
(6, '工单管理系统', 'WORK_ORDER', 'JWT', '🎫', NULL, 'ACTIVE'),
(7, '评价管理系统', 'EVALUATION', 'JWT', '⭐', NULL, 'ACTIVE');

-- ================================================================
-- 26. 系统配置表
-- ================================================================
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(512) COMMENT '配置描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ================================================================
-- 27. 权益商品表
-- ================================================================
CREATE TABLE benefit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
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
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_benefit_code (benefit_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权益商品表';

COMMIT;