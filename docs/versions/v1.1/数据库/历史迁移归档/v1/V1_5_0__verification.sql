CREATE TABLE pickup_no_sequence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '取餐号序列ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    business_date DATE NOT NULL COMMENT '营业日期',
    current_value INT NOT NULL COMMENT '当前取餐号数值',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_pickup_no_sequence_day (tenant_id, store_id, business_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='营业日取餐号序列表';

CREATE TABLE verification_credential (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '核销凭证ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    business_type VARCHAR(32) NOT NULL COMMENT '关联业务类型',
    business_no VARCHAR(64) NOT NULL COMMENT '关联业务单号',
    summary VARCHAR(255) NOT NULL COMMENT '凭证摘要',
    business_date DATE NOT NULL COMMENT '营业日期',
    pickup_no VARCHAR(16) NULL COMMENT '取餐号',
    token_hash VARCHAR(128) NOT NULL COMMENT '不可逆核销令牌哈希',
    status VARCHAR(32) NOT NULL COMMENT '凭证状态',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_verification_business (tenant_id, store_id, business_type, business_no),
    UNIQUE KEY uk_verification_token (token_hash),
    KEY idx_verification_pickup (tenant_id, store_id, business_date, pickup_no),
    KEY idx_verification_status (tenant_id, store_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='不可猜测核销凭证表';

CREATE TABLE verification_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '核销记录ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    business_type VARCHAR(32) NOT NULL COMMENT '关联业务类型',
    business_no VARCHAR(64) NOT NULL COMMENT '关联业务单号',
    summary VARCHAR(255) NOT NULL COMMENT '核销摘要',
    operator_user_id BIGINT NOT NULL COMMENT '核销操作人用户ID',
    reason VARCHAR(255) NOT NULL COMMENT '核销原因',
    verified_at DATETIME(3) NOT NULL COMMENT '核销时间',
    UNIQUE KEY uk_verification_record_once (tenant_id, store_id, business_type, business_no),
    KEY idx_verification_record_time (tenant_id, store_id, verified_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='核销事实审计记录表';
