CREATE TABLE service_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '服务项目ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(128) NOT NULL COMMENT '服务项目名称',
    payment_mode VARCHAR(32) NOT NULL COMMENT '支付模式',
    price_cent INT NOT NULL COMMENT '服务价格，单位分',
    duration_minutes INT NOT NULL COMMENT '服务时长，单位分钟',
    status VARCHAR(32) NOT NULL COMMENT '服务项目状态',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_service_item_name (tenant_id, store_id, name),
    KEY idx_service_item_store_status (tenant_id, store_id, status),
    CONSTRAINT chk_service_item_price CHECK (price_cent >= 0),
    CONSTRAINT chk_service_item_duration CHECK (duration_minutes > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='可预约服务项目表';

CREATE TABLE service_slot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '服务时段ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    service_id BIGINT NOT NULL COMMENT '服务项目ID',
    service_date DATE NOT NULL COMMENT '服务日期',
    start_time VARCHAR(16) NOT NULL COMMENT '开始时间',
    end_time VARCHAR(16) NOT NULL COMMENT '结束时间',
    capacity INT NOT NULL COMMENT '可预约容量',
    used_capacity INT NOT NULL DEFAULT 0 COMMENT '已占用容量',
    paused TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否暂停预约',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_service_slot_time (tenant_id, store_id, service_id, service_date, start_time, end_time),
    KEY idx_service_slot_date (tenant_id, store_id, service_date),
    CONSTRAINT chk_service_slot_capacity CHECK (capacity > 0 AND used_capacity >= 0 AND used_capacity <= capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务预约时段容量表';

CREATE TABLE service_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '服务预约ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    service_id BIGINT NOT NULL COMMENT '服务项目ID',
    slot_id BIGINT NOT NULL COMMENT '服务时段ID',
    reservation_no VARCHAR(64) NOT NULL COMMENT '预约单号',
    status VARCHAR(32) NOT NULL COMMENT '预约状态',
    contact_mobile VARCHAR(32) NOT NULL COMMENT '联系人手机号',
    voucher_code VARCHAR(128) NULL COMMENT '预约凭证码',
    transaction_id VARCHAR(128) NULL COMMENT '微信支付交易号',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_service_reservation_no (tenant_id, reservation_no),
    KEY idx_service_reservation_slot (tenant_id, store_id, slot_id, status),
    KEY idx_service_reservation_service (tenant_id, store_id, service_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='顾客服务预约记录表';

CREATE TABLE reservation_status_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约状态日志ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    reservation_no VARCHAR(64) NOT NULL COMMENT '预约单号',
    from_status VARCHAR(32) NULL COMMENT '变更前状态',
    to_status VARCHAR(32) NOT NULL COMMENT '变更后状态',
    reason VARCHAR(255) NOT NULL COMMENT '状态变更原因',
    operator_user_id BIGINT NULL COMMENT '操作人用户ID',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_reservation_status_log_no (tenant_id, store_id, reservation_no, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约状态变化日志表';
