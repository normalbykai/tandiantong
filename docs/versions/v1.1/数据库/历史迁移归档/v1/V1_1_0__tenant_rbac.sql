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
