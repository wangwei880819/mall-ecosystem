-- ============================================================
-- 商品审核模拟数据（供一级/二级选品审核验证）
-- 执行: mysql -u root -proot igou_mall < mock_products.sql
-- ============================================================

-- 一级选品审核待审核商品（status=PENDING, review_level=null）
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

-- 一级审核已通过，待二级审核商品（status=ONE_PASSED, review_level=1）
INSERT INTO product (product_code, merchant_id, product_name, product_type, category_id, category, price, market_price, stock, image_urls, status, review_level, level1_audit_time, level1_auditor, create_time) VALUES
('PRD2408009', 1, '京东PLUS会员年卡', 'DIGITAL', 5, '生活服务', 99.00, 128.00, 500, '/uploads/products/img9.png', 'ONE_PASSED', 1, '2024-08-15 10:30:00', '审核员', '2024-08-14 09:00:00'),
('PRD2408010', 2, '美团外卖会员月卡', 'DIGITAL', 5, '生活服务', 15.00, 20.00, 800, '/uploads/products/img10.png', 'ONE_PASSED', 1, '2024-08-15 14:00:00', '审核员', '2024-08-14 09:00:00'),
('PRD2408011', 3, 'Keep会员季卡', 'DIGITAL', 6, '运动健康', 68.00, 88.00, 250, '/uploads/products/img11.png', 'ONE_PASSED', 1, '2024-08-16 09:00:00', '审核员', '2024-08-15 09:00:00')
ON DUPLICATE KEY UPDATE product_code=product_code;