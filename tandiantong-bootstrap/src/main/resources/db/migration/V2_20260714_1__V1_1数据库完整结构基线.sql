-- V1.1 数据库完整结构基线
-- 创建时间：2026-07-14
-- 基线版本：2.20260714.1
-- 说明：本脚本汇总 V1 历史迁移链的已确认表结构、索引、约束、基础数据和注释，用于新库一次性初始化。
-- 旧库保留数据切换时必须先确认结构等价，再将 Flyway 历史基线标记到 2.20260714.1，禁止在旧库重复执行本脚本。

-- ===== 汇总来源：V1_0_0__foundation_baseline.sql =====
create table if not exists foundation_schema_marker
(
    id          bigint       not null primary key comment '主键ID',
    marker_name varchar(64)  not null comment '基础迁移标记名称',
    created_at  timestamp    not null default current_timestamp comment '创建时间',
    constraint uk_foundation_schema_marker_name unique (marker_name)
) comment = '数据库基础版本标记表';

insert into foundation_schema_marker (id, marker_name)
values (1, 'V1 工程基础迁移')
on duplicate key update marker_name = values(marker_name);

-- ===== 汇总来源：V1_1_0__tenant_rbac.sql =====
create table tenant
(
    id          bigint       not null primary key comment '租户ID',
    tenant_code varchar(64)  not null comment '租户编码',
    name        varchar(128) not null comment '租户名称',
    status      varchar(32)  not null comment '租户状态',
    created_at  timestamp    not null default current_timestamp comment '创建时间',
    updated_at  timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_tenant_code unique (tenant_code)
) comment = '商户租户主表';

create table store
(
    id          bigint       not null primary key comment '门店ID',
    tenant_id   bigint       not null comment '租户ID',
    name        varchar(128) not null comment '门店名称',
    status      varchar(32)  not null comment '门店状态',
    created_at  timestamp    not null default current_timestamp comment '创建时间',
    updated_at  timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_store_tenant_name unique (tenant_id, name),
    index idx_store_tenant (tenant_id)
) comment = '商户门店表';

create table platform_user
(
    id              bigint       not null primary key comment '平台用户ID',
    mobile          varchar(32)  not null comment '手机号',
    display_name    varchar(64)  not null comment '显示名称',
    password_hash   varchar(128) not null comment '密码哈希',
    status          varchar(32)  not null comment '用户状态',
    token_version   int          not null default 1 comment '令牌版本号',
    created_at      timestamp    not null default current_timestamp comment '创建时间',
    updated_at      timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_platform_user_mobile unique (mobile)
) comment = '平台管理员用户表';

create table admin_user
(
    id              bigint       not null primary key comment '后台用户ID',
    tenant_id       bigint       not null comment '租户ID',
    store_id        bigint       not null comment '门店ID',
    mobile          varchar(32)  not null comment '手机号',
    display_name    varchar(64)  not null comment '显示名称',
    password_hash   varchar(128) not null comment '密码哈希',
    status          varchar(32)  not null comment '用户状态',
    token_version   int          not null default 1 comment '令牌版本号',
    created_at      timestamp    not null default current_timestamp comment '创建时间',
    updated_at      timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_admin_user_tenant_mobile unique (tenant_id, mobile),
    index idx_admin_user_tenant_store (tenant_id, store_id)
) comment = '商户后台用户表';

create table role
(
    id          bigint       not null primary key comment '角色ID',
    tenant_id   bigint       null comment '租户ID，平台角色为空',
    domain      varchar(32)  not null comment '权限域',
    name        varchar(64)  not null comment '角色名称',
    description varchar(255) null comment '角色说明',
    system_role tinyint(1)   not null default 0 comment '是否系统内置角色',
    created_at  timestamp    not null default current_timestamp comment '创建时间',
    updated_at  timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_role_domain_tenant_name unique (domain, tenant_id, name),
    index idx_role_tenant (tenant_id)
) comment = '角色定义表';

create table permission
(
    id              bigint       not null primary key comment '权限ID',
    domain          varchar(32)  not null comment '权限域',
    permission_type varchar(32)  not null comment '权限类型',
    permission_code varchar(128) not null comment '权限编码',
    name            varchar(64)  not null comment '权限名称',
    created_at      timestamp    not null default current_timestamp comment '创建时间',
    constraint uk_permission_domain_code unique (domain, permission_code)
) comment = '权限定义表';

create table user_role
(
    id          bigint      not null primary key comment '用户角色关联ID',
    tenant_id   bigint      null comment '租户ID，平台用户为空',
    domain      varchar(32) not null comment '权限域',
    user_id     bigint      not null comment '用户ID',
    role_id     bigint      not null comment '角色ID',
    created_at  timestamp   not null default current_timestamp comment '创建时间',
    constraint uk_user_role_domain_tenant_user_role unique (domain, tenant_id, user_id, role_id),
    index idx_user_role_tenant_user (tenant_id, user_id)
) comment = '用户角色关联表';

create table role_permission
(
    id            bigint      not null primary key comment '角色权限关联ID',
    tenant_id     bigint      null comment '租户ID，平台角色为空',
    domain        varchar(32) not null comment '权限域',
    role_id       bigint      not null comment '角色ID',
    permission_id bigint      not null comment '权限ID',
    created_at    timestamp   not null default current_timestamp comment '创建时间',
    constraint uk_role_permission_domain_tenant_role_perm unique (domain, tenant_id, role_id, permission_id),
    index idx_role_permission_tenant_role (tenant_id, role_id)
) comment = '角色权限关联表';

create table operation_log
(
    id             bigint       not null primary key comment '操作日志ID',
    tenant_id      bigint       null comment '租户ID',
    store_id       bigint       null comment '门店ID',
    domain         varchar(32)  not null comment '操作权限域',
    operator_id    bigint       not null comment '操作人用户ID',
    operation_type varchar(64)  not null comment '操作类型',
    target_type    varchar(64)  not null comment '操作对象类型',
    target_id      varchar(64)  null comment '操作对象ID',
    detail         varchar(1024) null comment '操作详情',
    trace_id       varchar(128) null comment '请求追踪号',
    created_at     timestamp    not null default current_timestamp comment '创建时间',
    index idx_operation_log_tenant_time (tenant_id, created_at),
    index idx_operation_log_operator_time (operator_id, created_at)
) comment = '后台关键操作审计日志表';

-- ===== 汇总来源：V1_2_0__catalog_inventory.sql =====
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

-- ===== 汇总来源：V1_3_0__order_payment.sql =====
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

-- ===== 汇总来源：V1_4_0__reservation.sql =====
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

-- ===== 汇总来源：V1_5_0__verification.sql =====
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

-- ===== 汇总来源：V1_6_0__analytics.sql =====
CREATE TABLE analytics_order_fact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单统计事实ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    business_date DATE NOT NULL COMMENT '营业日期',
    status VARCHAR(32) NOT NULL COMMENT '订单状态',
    gross_amount_cent INT NOT NULL COMMENT '订单毛收入金额，单位分',
    refund_amount_cent INT NOT NULL DEFAULT 0 COMMENT '退款金额，单位分',
    quantity INT NOT NULL COMMENT '商品数量',
    product_name VARCHAR(128) NOT NULL COMMENT '商品名称快照',
    sku_name VARCHAR(128) NOT NULL COMMENT 'SKU名称快照',
    addon_names VARCHAR(512) NULL COMMENT '加料名称快照',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_analytics_order_fact_no (tenant_id, store_id, order_no, product_name, sku_name),
    KEY idx_analytics_order_fact_date (tenant_id, store_id, business_date, status),
    KEY idx_analytics_order_fact_product (tenant_id, store_id, product_name, business_date),
    CONSTRAINT chk_analytics_order_amount CHECK (gross_amount_cent >= 0 AND refund_amount_cent >= 0),
    CONSTRAINT chk_analytics_order_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单经营统计事实表';

CREATE TABLE analytics_reservation_fact (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约统计事实ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    reservation_no VARCHAR(64) NOT NULL COMMENT '预约单号',
    business_date DATE NOT NULL COMMENT '营业日期',
    status VARCHAR(32) NOT NULL COMMENT '预约状态',
    slot_capacity INT NOT NULL COMMENT '时段总容量',
    slot_used_capacity INT NOT NULL COMMENT '时段已用容量',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_analytics_reservation_no (tenant_id, store_id, reservation_no),
    KEY idx_analytics_reservation_date (tenant_id, store_id, business_date, status),
    CONSTRAINT chk_analytics_reservation_capacity CHECK (slot_capacity > 0 AND slot_used_capacity >= 0 AND slot_used_capacity <= slot_capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='预约经营统计事实表';

CREATE TABLE analytics_export_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '导出任务ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    export_type VARCHAR(32) NOT NULL COMMENT '导出类型',
    start_date DATE NOT NULL COMMENT '导出开始日期',
    end_date DATE NOT NULL COMMENT '导出结束日期',
    file_name VARCHAR(255) NOT NULL COMMENT '导出文件名',
    status VARCHAR(32) NOT NULL COMMENT '导出任务状态',
    operator_user_id BIGINT NOT NULL COMMENT '操作人用户ID',
    operator_contact_masked VARCHAR(128) NOT NULL COMMENT '脱敏后的操作人联系方式',
    audit_message VARCHAR(255) NOT NULL COMMENT '审计说明',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    finished_at DATETIME(3) NULL COMMENT '完成时间',
    KEY idx_analytics_export_task_tenant (tenant_id, store_id, created_at),
    KEY idx_analytics_export_task_status (tenant_id, store_id, status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经营数据导出审计任务表';

-- ===== 汇总来源：V1_7_0__merchant_provisioning.sql =====
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

-- ===== 汇总来源：V1_8_0__tenant_store_id_generation.sql =====
ALTER TABLE tenant MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户ID';
ALTER TABLE store MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '门店ID';

-- ===== 汇总来源：V1_9_0__database_chinese_comments.sql =====
-- 本迁移只补充数据库对象中文含义，不修改既有字段类型、约束和业务数据。
ALTER TABLE foundation_schema_marker COMMENT = '数据库基础版本标记表';
ALTER TABLE tenant COMMENT = '商户租户主表';
ALTER TABLE store COMMENT = '商户门店表';
ALTER TABLE platform_user COMMENT = '平台管理员用户表';
ALTER TABLE admin_user COMMENT = '商户后台用户表';
ALTER TABLE role COMMENT = '角色定义表';
ALTER TABLE permission COMMENT = '权限定义表';
ALTER TABLE user_role COMMENT = '用户角色关联表';
ALTER TABLE role_permission COMMENT = '角色权限关联表';
ALTER TABLE operation_log COMMENT = '后台关键操作审计日志表';

ALTER TABLE product_category COMMENT = '商品分类表';
ALTER TABLE product COMMENT = '商品主表';
ALTER TABLE product_sku COMMENT = '商品SKU与库存表';
ALTER TABLE addon_group COMMENT = '商品加料分组表';
ALTER TABLE addon_option COMMENT = '商品加料选项表';
ALTER TABLE product_addon_relation COMMENT = '商品与加料分组关联表';
ALTER TABLE inventory_record COMMENT = '库存变化流水表';

ALTER TABLE sales_order COMMENT = '商品销售订单主表';
ALTER TABLE sales_order_item COMMENT = '商品销售订单明细快照表';
ALTER TABLE payment_record COMMENT = '订单支付记录表';
ALTER TABLE refund_record COMMENT = '订单整单退款记录表';
ALTER TABLE order_status_log COMMENT = '订单状态变化日志表';
ALTER TABLE business_idempotency_record COMMENT = '业务请求幂等记录表';

ALTER TABLE service_item COMMENT = '可预约服务项目表';
ALTER TABLE service_slot COMMENT = '服务预约时段容量表';
ALTER TABLE service_reservation COMMENT = '顾客服务预约记录表';
ALTER TABLE reservation_status_log COMMENT = '预约状态变化日志表';

ALTER TABLE pickup_no_sequence COMMENT = '营业日取餐号序列表';
ALTER TABLE verification_credential COMMENT = '不可猜测核销凭证表';
ALTER TABLE verification_record COMMENT = '核销事实审计记录表';

ALTER TABLE analytics_order_fact COMMENT = '订单经营统计事实表';
ALTER TABLE analytics_reservation_fact COMMENT = '预约经营统计事实表';
ALTER TABLE analytics_export_task COMMENT = '经营数据导出审计任务表';

ALTER TABLE merchant_invitation COMMENT = '商户管理员激活邀请表';
ALTER TABLE tenant_payment_config COMMENT = '租户门店支付配置状态表';
ALTER TABLE mini_program_scene COMMENT = '小程序入口码与租户门店映射表';

-- ===== 汇总来源：V1_10_0__idempotency_and_flyway_comments.sql =====
ALTER TABLE business_idempotency_record COMMENT = '业务请求幂等记录表';

ALTER TABLE business_idempotency_record
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '幂等记录ID',
    MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '租户ID',
    MODIFY COLUMN idempotency_key VARCHAR(128) NOT NULL COMMENT '幂等键',
    MODIFY COLUMN business_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    MODIFY COLUMN business_no VARCHAR(64) NOT NULL COMMENT '业务单号',
    MODIFY COLUMN result_status VARCHAR(32) NOT NULL COMMENT '业务处理结果状态',
    MODIFY COLUMN created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间';

ALTER TABLE flyway_schema_history COMMENT = 'Flyway数据库迁移历史表';

ALTER TABLE flyway_schema_history
    MODIFY COLUMN installed_rank INT NOT NULL COMMENT '迁移安装顺序',
    MODIFY COLUMN version VARCHAR(50) NULL COMMENT '迁移版本号',
    MODIFY COLUMN description VARCHAR(200) NOT NULL COMMENT '迁移描述',
    MODIFY COLUMN type VARCHAR(20) NOT NULL COMMENT '迁移类型',
    MODIFY COLUMN script VARCHAR(1000) NOT NULL COMMENT '迁移脚本名称',
    MODIFY COLUMN checksum INT NULL COMMENT '迁移脚本校验值',
    MODIFY COLUMN installed_by VARCHAR(100) NOT NULL COMMENT '执行迁移的数据库用户',
    MODIFY COLUMN installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '迁移安装时间',
    MODIFY COLUMN execution_time INT NOT NULL COMMENT '迁移执行耗时，单位毫秒',
    MODIFY COLUMN success TINYINT(1) NOT NULL COMMENT '迁移是否成功';
