-- 本迁移只补充数据库对象中文含义，不修改既有字段类型、约束和业务数据。
ALTER TABLE foundation_schema_marker COMMENT = '数据库基础版本标记表';
ALTER TABLE tenant COMMENT = '商户租户主表';
ALTER TABLE store COMMENT = '商户门店表';
ALTER TABLE platform_user COMMENT = '平台管理员用户表';
ALTER TABLE admin_user COMMENT = '商户后台用户表';
ALTER TABLE role COMMENT = '角色定义表';
ALTER TABLE permission COMMENT = '权限定义表';
ALTER TABLE user_role COMMENT = '用户角色关联表';
ALTER TABLE role_permission COMMENT = '角色权限关联表';
ALTER TABLE operation_log COMMENT = '后台关键操作审计日志表';

ALTER TABLE product_category COMMENT = '商品分类表';
ALTER TABLE product COMMENT = '商品主表';
ALTER TABLE product_sku COMMENT = '商品SKU与库存表';
ALTER TABLE addon_group COMMENT = '商品加料分组表';
ALTER TABLE addon_option COMMENT = '商品加料选项表';
ALTER TABLE product_addon_relation COMMENT = '商品与加料分组关联表';
ALTER TABLE inventory_record COMMENT = '库存变化流水表';

ALTER TABLE sales_order COMMENT = '商品销售订单主表';
ALTER TABLE sales_order_item COMMENT = '商品销售订单明细快照表';
ALTER TABLE payment_record COMMENT = '订单支付记录表';
ALTER TABLE refund_record COMMENT = '订单整单退款记录表';
ALTER TABLE order_status_log COMMENT = '订单状态变化日志表';
ALTER TABLE business_idempotency_record COMMENT = '业务请求幂等记录表';

ALTER TABLE service_item COMMENT = '可预约服务项目表';
ALTER TABLE service_slot COMMENT = '服务预约时段容量表';
ALTER TABLE service_reservation COMMENT = '顾客服务预约记录表';
ALTER TABLE reservation_status_log COMMENT = '预约状态变化日志表';

ALTER TABLE pickup_no_sequence COMMENT = '营业日取餐号序列表';
ALTER TABLE verification_credential COMMENT = '不可猜测核销凭证表';
ALTER TABLE verification_record COMMENT = '核销事实审计记录表';

ALTER TABLE analytics_order_fact COMMENT = '订单经营统计事实表';
ALTER TABLE analytics_reservation_fact COMMENT = '预约经营统计事实表';
ALTER TABLE analytics_export_task COMMENT = '经营数据导出审计任务表';

ALTER TABLE merchant_invitation COMMENT = '商户管理员激活邀请表';
ALTER TABLE tenant_payment_config COMMENT = '租户门店支付配置状态表';
ALTER TABLE mini_program_scene COMMENT = '小程序入口码与租户门店映射表';
