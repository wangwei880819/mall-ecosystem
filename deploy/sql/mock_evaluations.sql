-- ============================================================
-- 模拟评价数据（为 EVALUATED 状态的订单生成多维评价）
-- 执行: mysql -u igou -p igou_mall < mock_evaluations.sql
-- ============================================================

-- 清理旧评价数据（可选，仅清理模拟数据）
DELETE FROM evaluation WHERE order_id IN (SELECT id FROM mall_order WHERE order_code LIKE 'ORD1786900000%');

-- 使用存储过程批量插入模拟评价
-- 每个订单的评价包含：商品质量、物流配送、服务态度、售后服务、性价比 五个维度，以及评价内容、标签、情感分析

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  '非常好用！开通秒到账，画质清晰无广告，强烈推荐！',
  '快速到账,画质清晰,无广告,性价比高',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 3 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000001';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 4, 5,
  '爱奇艺会员很划算，比官网便宜不少，到账很快，好评！',
  '价格实惠,到账快,画质好',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000002';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 4, 5, 5, 5,
  '瑞幸咖啡通兑券超值！29元券只要19.9，每天一杯不心疼',
  '超值优惠,配送快,包装好',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 1 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000003';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  4, 5, 4, 4, 5,
  'QQ音乐会员不错，音质提升明显，就是偶尔有卡顿，总体满意',
  '音质好,物流快,偶尔卡顿',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 3 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000004';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  4, 3, 4, 4, 3,
  '美团外卖红包还行，但红包金额有点小，用起来不太划算',
  '配送快,金额偏小,偶尔用用',
  'NEUTRAL', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000005';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  '王者荣耀点券秒到账！比官方充值划算多了，已经回购好几次了',
  '秒到账,价格优惠,官方正品,回购多次',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 1 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000006';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 4,
  '网易云音乐黑胶VIP音质很棒，就是价格比QQ音乐贵一点，但歌曲库更全',
  '音质好,歌曲库全,价格略高',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 3 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000007';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  '星巴克星礼卡88折入手，太划算了！送朋友也很有面子',
  '折扣力度大,送礼佳品,包装精美',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000008';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  4, 4, 5, 4, 4,
  '麦当劳套餐味道不错，配送也快，就是汉堡有点凉了',
  '味道好,配送快,温度不足',
  'NEUTRAL', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 1 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000009';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  'B站大会员太香了！追番必备，高清画质无广告，推荐！',
  '追番必备,高清画质,无广告,价格实惠',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000010';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 4,
  '滴滴快车券很方便，打车省了不少钱，就是券面额希望能再大些',
  '方便快捷,省钱实用,券面额偏小',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 3 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000011';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  '京东E卡95折，叠加AI豆抵扣只要90元，太划算了！',
  '折扣给力,官方正品,叠加优惠',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000012';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  4, 5, 4, 3, 4,
  '优酷会员还行，但广告还是有点多，售后客服响应也不够快',
  '价格实惠,广告偏多,客服一般',
  'NEUTRAL', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 1 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000013';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  '滴滴专车券体验很好，车辆干净舒适，司机服务态度也棒',
  '车辆舒适,服务好,价格实惠',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000014';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 4, 5, 5, 5,
  '肯德基翅桶分量足，味道好，就是配送稍慢了点，等了40分钟',
  '分量足,味道好,配送稍慢',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 1 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000015';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  '第二次购买腾讯视频会员了，一如既往的好用，会继续回购',
  '回购多次,秒到账,画质清晰',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 3 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000016';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  '爱奇艺会员真不错，院线大片抢先看，全家都在用',
  '院线大片,全家人用,价格实惠',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000017';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  4, 4, 4, 4, 4,
  '瑞幸咖啡券买了4张，有两张扫码时提示已用过，联系客服处理中',
  '价格实惠,部分有问题,客服处理中',
  'NEUTRAL', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 1 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000018';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  5, 5, 5, 5, 5,
  'QQ音乐绿钻音质没得说，听歌必备，价格也合理',
  '音质好,听歌必备,价格合理',
  'POSITIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 2 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000019';

INSERT INTO evaluation (order_id, merchant_id, product_id, customer_id, customer_phone, score_quality, score_delivery, score_service, score_aftersale, score_value, content, tags, sentiment, ai_status, create_time)
SELECT o.id, o.merchant_id, o.product_id, o.customer_id, o.customer_phone,
  3, 2, 3, 2, 3,
  '美团红包这次体验不太好，有一个红包过期了不能用，找客服也没解决',
  '体验差,红包过期,客服不处理',
  'NEGATIVE', 'AUTO_PASS',
  DATE_ADD(o.create_time, INTERVAL 1 DAY)
FROM mall_order o WHERE o.order_code = 'ORD178690000020';

-- 确认插入结果
SELECT COUNT(*) AS evaluation_count FROM evaluation WHERE order_id IN (SELECT id FROM mall_order WHERE order_code LIKE 'ORD1786900000%');