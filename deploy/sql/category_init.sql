-- ============================================================
-- 分类管理（product_category）初始化数据脚本
-- 生成时间: 2026-08-14
-- 执行: mysql -u igou -p igou_mall < category_init.sql
-- 说明: 使用显式ID保证父子关系正确；可重复执行（ON DUPLICATE KEY UPDATE）
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
