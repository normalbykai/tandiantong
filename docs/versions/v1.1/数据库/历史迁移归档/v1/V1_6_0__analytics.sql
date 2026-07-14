CREATE TABLE analytics_order_fact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单统计事实ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    business_date DATE NOT NULL COMMENT '营业日期',
    status VARCHAR(32) NOT NULL COMMENT '订单状态',
    gross_amount_cent INT NOT NULL COMMENT '订单毛收入金额，单位分',
    refund_amount_cent INT NOT NULL DEFAULT 0 COMMENT '退款金额，单位分',
    quantity INT NOT NULL COMMENT '商品数量',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称快照',
    sku_name VARCHAR(128) NOT NULL COMMENT 'SKU名称快照',
    addon_names VARCHAR(512) NULL COMMENT '加料名称快照',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_analytics_order_fact_no (tenant_id, store_id, order_no, product_name, sku_name),
    KEY idx_analytics_order_fact_date (tenant_id, store_id, business_date, status),
    KEY idx_analytics_order_fact_product (tenant_id, store_id, product_name, business_date),
    CONSTRAINT chk_analytics_order_amount CHECK (gross_amount_cent >= 0 AND refund_amount_cent >= 0),
    CONSTRAINT chk_analytics_order_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单经营统计事实表';

CREATE TABLE analytics_reservation_fact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约统计事实ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    reservation_no VARCHAR(64) NOT NULL COMMENT '预约单号',
    business_date DATE NOT NULL COMMENT '营业日期',
    status VARCHAR(32) NOT NULL COMMENT '预约状态',
    slot_capacity INT NOT NULL COMMENT '时段总容量',
    slot_used_capacity INT NOT NULL COMMENT '时段已用容量',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_analytics_reservation_no (tenant_id, store_id, reservation_no),
    KEY idx_analytics_reservation_date (tenant_id, store_id, business_date, status),
    CONSTRAINT chk_analytics_reservation_capacity CHECK (slot_capacity > 0 AND slot_used_capacity >= 0 AND slot_used_capacity <= slot_capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约经营统计事实表';

CREATE TABLE analytics_export_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '导出任务ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    export_type VARCHAR(32) NOT NULL COMMENT '导出类型',
    start_date DATE NOT NULL COMMENT '导出开始日期',
    end_date DATE NOT NULL COMMENT '导出结束日期',
    file_name VARCHAR(255) NOT NULL COMMENT '导出文件名',
    status VARCHAR(32) NOT NULL COMMENT '导出任务状态',
    operator_user_id BIGINT NOT NULL COMMENT '操作人用户ID',
    operator_contact_masked VARCHAR(128) NOT NULL COMMENT '脱敏后的操作人联系方式',
    audit_message VARCHAR(255) NOT NULL COMMENT '审计说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    finished_at DATETIME(3) NULL COMMENT '完成时间',
    KEY idx_analytics_export_task_tenant (tenant_id, store_id, created_at),
    KEY idx_analytics_export_task_status (tenant_id, store_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营数据导出审计任务表';
