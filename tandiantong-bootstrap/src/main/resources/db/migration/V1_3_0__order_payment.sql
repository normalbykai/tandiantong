CREATE TABLE sales_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '销售订单ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    customer_id BIGINT NULL COMMENT '顾客用户ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    status VARCHAR(32) NOT NULL COMMENT '订单状态',
    pay_amount_cent INT NOT NULL COMMENT '应付金额，单位分',
    contact_mobile VARCHAR(32) NOT NULL COMMENT '联系人手机号',
    pickup_time_text VARCHAR(64) NOT NULL COMMENT '取餐时间说明',
    prepay_id VARCHAR(128) NULL COMMENT '微信预支付ID',
    paid_at DATETIME(3) NULL COMMENT '支付成功时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_sales_order_no (tenant_id, order_no),
    KEY idx_sales_order_store_status (tenant_id, store_id, status, created_at),
    CONSTRAINT chk_sales_order_amount CHECK (pay_amount_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品销售订单主表';

CREATE TABLE sales_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单明细ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    sku_id BIGINT NOT NULL COMMENT '商品SKU ID',
    product_name VARCHAR(128) NOT NULL COMMENT '下单时商品名称快照',
    sku_text VARCHAR(512) NOT NULL COMMENT '下单时SKU规格快照',
    addon_snapshot VARCHAR(1000) NULL COMMENT '下单时加料快照',
    unit_price_cent INT NOT NULL COMMENT '商品单价，单位分',
    addon_amount_cent INT NOT NULL COMMENT '单份加料金额，单位分',
    quantity INT NOT NULL COMMENT '购买数量',
    subtotal_cent INT NOT NULL COMMENT '明细小计金额，单位分',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_sales_order_item_order (tenant_id, store_id, order_no),
    CONSTRAINT chk_sales_order_item_amount CHECK (unit_price_cent >= 0 AND addon_amount_cent >= 0 AND subtotal_cent >= 0),
    CONSTRAINT chk_sales_order_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品销售订单明细快照表';

CREATE TABLE payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付记录ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    transaction_id VARCHAR(128) NOT NULL COMMENT '微信支付交易号',
    amount_cent INT NOT NULL COMMENT '支付金额，单位分',
    status VARCHAR(32) NOT NULL COMMENT '支付状态',
    paid_at DATETIME(3) NOT NULL COMMENT '支付完成时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_payment_transaction (tenant_id, transaction_id),
    UNIQUE KEY uk_payment_order (tenant_id, order_no),
    CONSTRAINT chk_payment_record_amount CHECK (amount_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单支付记录表';

CREATE TABLE refund_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '退款记录ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    refund_no VARCHAR(64) NOT NULL COMMENT '退款单号',
    amount_cent INT NOT NULL COMMENT '退款金额，单位分',
    status VARCHAR(32) NOT NULL COMMENT '退款状态',
    reason VARCHAR(255) NOT NULL COMMENT '退款原因',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_refund_no (tenant_id, refund_no),
    KEY idx_refund_order (tenant_id, store_id, order_no),
    CONSTRAINT chk_refund_record_amount CHECK (amount_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单整单退款记录表';

CREATE TABLE order_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单状态日志ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    from_status VARCHAR(32) NULL COMMENT '变更前状态',
    to_status VARCHAR(32) NOT NULL COMMENT '变更后状态',
    reason VARCHAR(255) NOT NULL COMMENT '状态变更原因',
    operator_user_id BIGINT NULL COMMENT '操作人用户ID',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_order_status_log_order (tenant_id, store_id, order_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态变化日志表';

CREATE TABLE business_idempotency_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '幂等记录ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    idempotency_key VARCHAR(128) NOT NULL COMMENT '幂等键',
    business_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    business_no VARCHAR(64) NOT NULL COMMENT '业务单号',
    result_status VARCHAR(32) NOT NULL COMMENT '业务处理结果状态',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_business_idempotency (tenant_id, business_type, idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务请求幂等记录表';
