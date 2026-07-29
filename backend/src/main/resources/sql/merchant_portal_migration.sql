-- 商户入驻平台 - 数据库变更脚本
-- 添加 merchant 表的 password 列

ALTER TABLE merchant ADD COLUMN password VARCHAR(100);
