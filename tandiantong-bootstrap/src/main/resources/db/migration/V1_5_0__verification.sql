CREATE TABLE pickup_no_sequence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    business_date DATE NOT NULL,
    current_value INT NOT NULL,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_pickup_no_sequence_day (tenant_id, store_id, business_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE verification_credential (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    business_no VARCHAR(64) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    business_date DATE NOT NULL,
    pickup_no VARCHAR(16) NULL,
    token_hash VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_verification_business (tenant_id, store_id, business_type, business_no),
    UNIQUE KEY uk_verification_token (token_hash),
    KEY idx_verification_pickup (tenant_id, store_id, business_date, pickup_no),
    KEY idx_verification_status (tenant_id, store_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE verification_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    business_type VARCHAR(32) NOT NULL,
    business_no VARCHAR(64) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    operator_user_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    verified_at DATETIME(3) NOT NULL,
    UNIQUE KEY uk_verification_record_once (tenant_id, store_id, business_type, business_no),
    KEY idx_verification_record_time (tenant_id, store_id, verified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
