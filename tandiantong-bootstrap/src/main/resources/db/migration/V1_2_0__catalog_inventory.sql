CREATE TABLE product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_product_category_name (tenant_id, store_id, name),
    KEY idx_product_category_store (tenant_id, store_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    name VARCHAR(128) NOT NULL,
    image_url VARCHAR(512) NULL,
    description VARCHAR(1000) NULL,
    base_price_cent INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_product_name (tenant_id, store_id, name),
    KEY idx_product_store_status (tenant_id, store_id, status, sort_order),
    KEY idx_product_category (tenant_id, store_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE product_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    specification_text VARCHAR(512) NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    price_cent INT NOT NULL,
    available_stock INT NOT NULL DEFAULT 0,
    locked_stock INT NOT NULL DEFAULT 0,
    warning_stock INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_product_sku_spec (tenant_id, store_id, product_id, specification_text),
    UNIQUE KEY uk_product_sku_code (tenant_id, store_id, sku_code),
    KEY idx_product_sku_product (tenant_id, store_id, product_id),
    CONSTRAINT chk_product_sku_stock CHECK (available_stock >= 0 AND locked_stock >= 0),
    CONSTRAINT chk_product_sku_price CHECK (price_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE addon_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    required TINYINT(1) NOT NULL DEFAULT 0,
    min_select INT NOT NULL DEFAULT 0,
    max_select INT NOT NULL DEFAULT 1,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_addon_group_name (tenant_id, store_id, name),
    CONSTRAINT chk_addon_group_select CHECK (min_select >= 0 AND max_select >= min_select)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE addon_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    addon_group_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    price_cent INT NOT NULL DEFAULT 0,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_addon_option_name (tenant_id, store_id, addon_group_id, name),
    KEY idx_addon_option_group (tenant_id, store_id, addon_group_id),
    CONSTRAINT chk_addon_option_price CHECK (price_cent >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE product_addon_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    addon_group_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_product_addon_relation (tenant_id, store_id, product_id, addon_group_id),
    KEY idx_product_addon_product (tenant_id, store_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE inventory_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    change_type VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    available_after INT NOT NULL,
    locked_after INT NOT NULL,
    business_no VARCHAR(64) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    operator_user_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_inventory_record_sku (tenant_id, store_id, sku_id, created_at),
    KEY idx_inventory_record_business (tenant_id, store_id, business_no),
    CONSTRAINT chk_inventory_record_quantity CHECK (quantity > 0),
    CONSTRAINT chk_inventory_record_stock CHECK (available_after >= 0 AND locked_after >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
