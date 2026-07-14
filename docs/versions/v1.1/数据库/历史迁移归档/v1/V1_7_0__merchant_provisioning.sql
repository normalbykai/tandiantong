CREATE TABLE merchant_invitation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商户邀请ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    admin_name VARCHAR(64) NOT NULL COMMENT '管理员姓名',
    admin_mobile VARCHAR(32) NOT NULL COMMENT '管理员手机号',
    invitation_code_hash VARCHAR(128) NOT NULL COMMENT '邀请码哈希',
    expires_at DATETIME(3) NOT NULL COMMENT '过期时间',
    used_at DATETIME(3) NULL COMMENT '使用时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_merchant_invitation_code (invitation_code_hash),
    KEY idx_merchant_invitation_tenant (tenant_id, store_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商户管理员激活邀请表';

CREATE TABLE tenant_payment_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付配置ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    status VARCHAR(32) NOT NULL COMMENT '支付配置状态',
    configured_at DATETIME(3) NULL COMMENT '配置完成时间',
    verified_at DATETIME(3) NULL COMMENT '验证通过时间',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_tenant_payment_config (tenant_id, store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户门店支付配置状态表';

CREATE TABLE mini_program_scene (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '小程序入口码ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    scene_key VARCHAR(128) NOT NULL COMMENT '入口场景键',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_mini_program_scene_key (scene_key),
    UNIQUE KEY uk_mini_program_scene_store (tenant_id, store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='小程序入口码与租户门店映射表';
