CREATE TABLE sales_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    customer_id BIGINT NULL,
    order_no VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    pay_amount_cent INT NOT NULL,
    contact_mobile VARCHAR(32) NOT NULL,
    pickup_time_text VARCHAR(64) NOT NULL,
    prepay_id VARCHAR(128) NULL,
    paid_at DATETIME(3) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_sales_order_no (tenant_id, order_no),
    KEY idx_sales_order_store_status (tenant_id, store_id, status, created_at),
    CONSTRAINT chk_sales_order_amount CHECK (pay_amount_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE sales_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    sku_id BIGINT NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    sku_text VARCHAR(512) NOT NULL,
    addon_snapshot VARCHAR(1000) NULL,
    unit_price_cent INT NOT NULL,
    addon_amount_cent INT NOT NULL,
    quantity INT NOT NULL,
    subtotal_cent INT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_sales_order_item_order (tenant_id, store_id, order_no),
    CONSTRAINT chk_sales_order_item_amount CHECK (unit_price_cent >= 0 AND addon_amount_cent >= 0 AND subtotal_cent >= 0),
    CONSTRAINT chk_sales_order_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    transaction_id VARCHAR(128) NOT NULL,
    amount_cent INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    paid_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_payment_transaction (tenant_id, transaction_id),
    UNIQUE KEY uk_payment_order (tenant_id, order_no),
    CONSTRAINT chk_payment_record_amount CHECK (amount_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE refund_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    refund_no VARCHAR(64) NOT NULL,
    amount_cent INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_refund_no (tenant_id, refund_no),
    KEY idx_refund_order (tenant_id, store_id, order_no),
    CONSTRAINT chk_refund_record_amount CHECK (amount_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE order_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    operator_user_id BIGINT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_order_status_log_order (tenant_id, store_id, order_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE business_idempotency_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    business_no VARCHAR(64) NOT NULL,
    result_status VARCHAR(32) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_business_idempotency (tenant_id, business_type, idempotency_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
