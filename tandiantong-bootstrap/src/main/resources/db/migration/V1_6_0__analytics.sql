CREATE TABLE analytics_order_fact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    gross_amount_cent INT NOT NULL,
    refund_amount_cent INT NOT NULL DEFAULT 0,
    quantity INT NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    sku_name VARCHAR(128) NOT NULL,
    addon_names VARCHAR(512) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_analytics_order_fact_no (tenant_id, store_id, order_no, product_name, sku_name),
    KEY idx_analytics_order_fact_date (tenant_id, store_id, business_date, status),
    KEY idx_analytics_order_fact_product (tenant_id, store_id, product_name, business_date),
    CONSTRAINT chk_analytics_order_amount CHECK (gross_amount_cent >= 0 AND refund_amount_cent >= 0),
    CONSTRAINT chk_analytics_order_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE analytics_reservation_fact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    reservation_no VARCHAR(64) NOT NULL,
    business_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    slot_capacity INT NOT NULL,
    slot_used_capacity INT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_analytics_reservation_no (tenant_id, store_id, reservation_no),
    KEY idx_analytics_reservation_date (tenant_id, store_id, business_date, status),
    CONSTRAINT chk_analytics_reservation_capacity CHECK (slot_capacity > 0 AND slot_used_capacity >= 0 AND slot_used_capacity <= slot_capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE analytics_export_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    export_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    operator_user_id BIGINT NOT NULL,
    operator_contact_masked VARCHAR(128) NOT NULL,
    audit_message VARCHAR(255) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    finished_at DATETIME(3) NULL,
    KEY idx_analytics_export_task_tenant (tenant_id, store_id, created_at),
    KEY idx_analytics_export_task_status (tenant_id, store_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
