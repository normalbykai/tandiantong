CREATE TABLE product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品分类ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_product_category_name (tenant_id, store_id, name),
    KEY idx_product_category_store (tenant_id, store_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    category_id BIGINT NULL COMMENT '商品分类ID',
    name VARCHAR(128) NOT NULL COMMENT '商品名称',
    image_url VARCHAR(512) NULL COMMENT '商品图片地址',
    description VARCHAR(1000) NULL COMMENT '商品描述',
    base_price_cent INT NOT NULL COMMENT '基础价格，单位分',
    status VARCHAR(32) NOT NULL COMMENT '商品状态',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_product_name (tenant_id, store_id, name),
    KEY idx_product_store_status (tenant_id, store_id, status, sort_order),
    KEY idx_product_category (tenant_id, store_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品主表';

CREATE TABLE product_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品SKU ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    specification_text VARCHAR(512) NOT NULL COMMENT '规格描述',
    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    price_cent INT NOT NULL COMMENT '销售价格，单位分',
    available_stock INT NOT NULL DEFAULT 0 COMMENT '可售库存',
    locked_stock INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
    warning_stock INT NOT NULL DEFAULT 0 COMMENT '库存预警数量',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_product_sku_spec (tenant_id, store_id, product_id, specification_text),
    UNIQUE KEY uk_product_sku_code (tenant_id, store_id, sku_code),
    KEY idx_product_sku_product (tenant_id, store_id, product_id),
    CONSTRAINT chk_product_sku_stock CHECK (available_stock >= 0 AND locked_stock >= 0),
    CONSTRAINT chk_product_sku_price CHECK (price_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU与库存表';

CREATE TABLE addon_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '加料分组ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    name VARCHAR(64) NOT NULL COMMENT '加料分组名称',
    required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否必选',
    min_select INT NOT NULL DEFAULT 0 COMMENT '最少选择数量',
    max_select INT NOT NULL DEFAULT 1 COMMENT '最多选择数量',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_addon_group_name (tenant_id, store_id, name),
    CONSTRAINT chk_addon_group_select CHECK (min_select >= 0 AND max_select >= min_select)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品加料分组表';

CREATE TABLE addon_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '加料选项ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    addon_group_id BIGINT NOT NULL COMMENT '加料分组ID',
    name VARCHAR(64) NOT NULL COMMENT '加料选项名称',
    price_cent INT NOT NULL DEFAULT 0 COMMENT '加价金额，单位分',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_addon_option_name (tenant_id, store_id, addon_group_id, name),
    KEY idx_addon_option_group (tenant_id, store_id, addon_group_id),
    CONSTRAINT chk_addon_option_price CHECK (price_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品加料选项表';

CREATE TABLE product_addon_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品加料关联ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    addon_group_id BIGINT NOT NULL COMMENT '加料分组ID',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_product_addon_relation (tenant_id, store_id, product_id, addon_group_id),
    KEY idx_product_addon_product (tenant_id, store_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品与加料分组关联表';

CREATE TABLE inventory_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存流水ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    sku_id BIGINT NOT NULL COMMENT '商品SKU ID',
    change_type VARCHAR(32) NOT NULL COMMENT '库存变化类型',
    quantity INT NOT NULL COMMENT '变化数量',
    available_after INT NOT NULL COMMENT '变化后可售库存',
    locked_after INT NOT NULL COMMENT '变化后锁定库存',
    business_no VARCHAR(64) NOT NULL COMMENT '关联业务单号',
    reason VARCHAR(255) NOT NULL COMMENT '变化原因',
    operator_user_id BIGINT NOT NULL COMMENT '操作人用户ID',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_inventory_record_sku (tenant_id, store_id, sku_id, created_at),
    KEY idx_inventory_record_business (tenant_id, store_id, business_no),
    CONSTRAINT chk_inventory_record_quantity CHECK (quantity > 0),
    CONSTRAINT chk_inventory_record_stock CHECK (available_after >= 0 AND locked_after >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存变化流水表';
