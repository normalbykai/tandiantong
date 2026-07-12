CREATE TABLE service_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    payment_mode VARCHAR(32) NOT NULL,
    price_cent INT NOT NULL,
    duration_minutes INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_service_item_name (tenant_id, store_id, name),
    KEY idx_service_item_store_status (tenant_id, store_id, status),
    CONSTRAINT chk_service_item_price CHECK (price_cent >= 0),
    CONSTRAINT chk_service_item_duration CHECK (duration_minutes > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE service_slot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    start_time VARCHAR(16) NOT NULL,
    end_time VARCHAR(16) NOT NULL,
    capacity INT NOT NULL,
    used_capacity INT NOT NULL DEFAULT 0,
    paused TINYINT(1) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_service_slot_time (tenant_id, store_id, service_id, service_date, start_time, end_time),
    KEY idx_service_slot_date (tenant_id, store_id, service_date),
    CONSTRAINT chk_service_slot_capacity CHECK (capacity > 0 AND used_capacity >= 0 AND used_capacity <= capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE service_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    reservation_no VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    contact_mobile VARCHAR(32) NOT NULL,
    voucher_code VARCHAR(128) NULL,
    transaction_id VARCHAR(128) NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_service_reservation_no (tenant_id, reservation_no),
    KEY idx_service_reservation_slot (tenant_id, store_id, slot_id, status),
    KEY idx_service_reservation_service (tenant_id, store_id, service_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservation_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    reservation_no VARCHAR(64) NOT NULL,
    from_status VARCHAR(32) NULL,
    to_status VARCHAR(32) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    operator_user_id BIGINT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_reservation_status_log_no (tenant_id, store_id, reservation_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
