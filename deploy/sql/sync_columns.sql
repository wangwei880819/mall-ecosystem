-- ============================================================
-- 商城生态运营管理平台 — 缺失列补齐脚本（幂等版本）
-- 生成时间: 2026-08-14 / 更新: 2026-08-17
-- 用途: 补齐测试环境旧表缺失的列（与后端实体/Mapper 对齐）
-- 执行: mysql -u igou -p igou_mall < sync_columns.sql
-- 说明: 可重复执行，列已存在时自动跳过
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

-- ========== 1. 商户表 merchant ==========
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

-- ========== 2. 商品表 product ==========
CALL safe_add_column('product', 'merchant_name', 'VARCHAR(128) COMMENT ''商户名称''');
CALL safe_add_column('product', 'vip_price', 'DECIMAL(10,2) COMMENT ''会员价''');
CALL safe_add_column('product', 'reject_reason', 'VARCHAR(500) COMMENT ''驳回原因''');
CALL safe_add_column('product', 'is_hot', 'TINYINT DEFAULT 0 COMMENT ''是否热销''');
CALL safe_add_column('product', 'is_new', 'TINYINT DEFAULT 0 COMMENT ''是否新品''');
CALL safe_add_column('product', 'is_recommend', 'TINYINT DEFAULT 0 COMMENT ''是否推荐''');
CALL safe_add_column('product', 'sort_order', 'INT DEFAULT 0 COMMENT ''排序号''');
CALL safe_add_column('product', 'update_time', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''');
CALL safe_add_column('product', 'approve_reason', 'VARCHAR(500) COMMENT ''审核通过说明''');
CALL safe_add_column('product', 'auditor', 'VARCHAR(64) COMMENT ''审核人''');
CALL safe_add_column('product', 'audit_time', 'DATETIME COMMENT ''审核时间''');

-- ========== 3. 客户表 customer ==========
CALL safe_add_column('customer', 'avatar', 'VARCHAR(512) COMMENT ''头像URL''');
CALL safe_add_column('customer', 'email', 'VARCHAR(128) COMMENT ''邮箱''');
CALL safe_add_column('customer', 'total_amount', 'DECIMAL(12,2) DEFAULT 0.00 COMMENT ''累计消费金额''');
CALL safe_add_column('customer', 'order_count', 'INT DEFAULT 0 COMMENT ''累计订单数''');
CALL safe_add_column('customer', 'birth_date', 'DATETIME COMMENT ''出生日期''');
CALL safe_add_column('customer', 'gender', 'VARCHAR(16) COMMENT ''性别''');

-- ========== 4. 订单表 mall_order ==========
CALL safe_add_column('mall_order', 'product_price', 'DECIMAL(10,2) COMMENT ''商品单价''');
CALL safe_add_column('mall_order', 'refund_status', 'VARCHAR(32) COMMENT ''退款状态''');
CALL safe_add_column('mall_order', 'delivery_status', 'VARCHAR(32) COMMENT ''配送状态''');
CALL safe_add_column('mall_order', 'address_id', 'BIGINT COMMENT ''收货地址ID''');

-- ========== 5. 客户标签表 customer_tag ==========
CALL safe_add_column('customer_tag', 'tag_type', 'VARCHAR(64) COMMENT ''标签类型''');

-- ========== 6. 评价表 evaluation（处理 user_phone→customer_phone 重命名 + 补齐缺失列） ==========
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

SELECT 'sync_columns.sql completed!' AS result;