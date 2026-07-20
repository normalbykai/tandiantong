-- 初始化平台通用字典数据：将系统中硬编码的业务状态枚举值迁入字典管理。
-- item_value 存储 API 返回的实际值（即 Java 枚举名，大写+下划线），
-- 前端通过 item_value 匹配 API 数据，获取 item_label 显示名称。

-- 1. 通用启停状态
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('ENABLE_STATUS', 'ENABLED',  'ENABLED',  '启用', 10, 'ENABLED'),
('ENABLE_STATUS', 'DISABLED', 'DISABLED', '停用', 20, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);

-- 2. 租户状态（API 层合并为三种简化状态）
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('TENANT_STATUS', 'PENDING',  'PENDING',  '待启用', 20, 'ENABLED'),
('TENANT_STATUS', 'ENABLED',  'ENABLED',  '已启用', 50, 'ENABLED'),
('TENANT_STATUS', 'DISABLED', 'DISABLED', '已停用', 60, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);

-- 3. 订单状态
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('ORDER_STATUS', 'PENDING_PAYMENT', 'PENDING_PAYMENT', '待支付', 10, 'ENABLED'),
('ORDER_STATUS', 'PENDING_VERIFY',  'PENDING_VERIFY',  '待核销', 20, 'ENABLED'),
('ORDER_STATUS', 'COMPLETED',       'COMPLETED',       '已完成', 30, 'ENABLED'),
('ORDER_STATUS', 'CANCELED',        'CANCELED',        '已取消', 40, 'ENABLED'),
('ORDER_STATUS', 'REFUNDING',       'REFUNDING',       '退款中', 50, 'ENABLED'),
('ORDER_STATUS', 'REFUNDED',        'REFUNDED',        '已退款', 60, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);

-- 4. 退款状态
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('REFUND_STATUS', 'PROCESSING', 'PROCESSING', '处理中', 10, 'ENABLED'),
('REFUND_STATUS', 'SUCCESS',    'SUCCESS',    '已成功', 20, 'ENABLED'),
('REFUND_STATUS', 'FAILED',     'FAILED',     '已失败', 30, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);

-- 5. 商品状态
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('PRODUCT_STATUS', 'DRAFT',     'DRAFT',     '草稿',   10, 'ENABLED'),
('PRODUCT_STATUS', 'ON_SHELF',  'ON_SHELF',  '已上架', 20, 'ENABLED'),
('PRODUCT_STATUS', 'OFF_SHELF', 'OFF_SHELF', '已下架', 30, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);

-- 6. 预约状态
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('RESERVATION_STATUS', 'PENDING_PAYMENT', 'PENDING_PAYMENT', '待支付', 10, 'ENABLED'),
('RESERVATION_STATUS', 'CONFIRMED',       'CONFIRMED',       '已确认', 20, 'ENABLED'),
('RESERVATION_STATUS', 'FULFILLED',       'FULFILLED',       '已履约', 30, 'ENABLED'),
('RESERVATION_STATUS', 'CANCELED',        'CANCELED',        '已取消', 40, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);

-- 7. 核销状态
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('VERIFICATION_STATUS', 'PENDING',  'PENDING',  '待核销', 10, 'ENABLED'),
('VERIFICATION_STATUS', 'VERIFIED', 'VERIFIED', '已核销', 20, 'ENABLED'),
('VERIFICATION_STATUS', 'CANCELED', 'CANCELED', '已取消', 30, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);

-- 8. 支付配置状态
insert into platform_dictionary_item (dictionary_type, item_code, item_value, item_label, sort_order, status) values
('PAYMENT_CONFIG_STATUS', 'NOT_CONFIGURED', 'NOT_CONFIGURED', '未配置',   10, 'ENABLED'),
('PAYMENT_CONFIG_STATUS', 'PENDING_VERIFY', 'PENDING_VERIFY', '待验证',   20, 'ENABLED'),
('PAYMENT_CONFIG_STATUS', 'VERIFIED',       'VERIFIED',       '已验证',   30, 'ENABLED'),
('PAYMENT_CONFIG_STATUS', 'VERIFY_FAILED',  'VERIFY_FAILED',  '验证失败', 40, 'ENABLED')
on duplicate key update item_value = values(item_value), item_label = values(item_label), sort_order = values(sort_order);
