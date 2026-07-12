create table tenant
(
    id          bigint       not null primary key,
    tenant_code varchar(64)  not null,
    name        varchar(128) not null,
    status      varchar(32)  not null,
    created_at  timestamp    not null default current_timestamp,
    updated_at  timestamp    not null default current_timestamp on update current_timestamp,
    constraint uk_tenant_code unique (tenant_code)
);

create table store
(
    id          bigint       not null primary key,
    tenant_id   bigint       not null,
    name        varchar(128) not null,
    status      varchar(32)  not null,
    created_at  timestamp    not null default current_timestamp,
    updated_at  timestamp    not null default current_timestamp on update current_timestamp,
    constraint uk_store_tenant_name unique (tenant_id, name),
    index idx_store_tenant (tenant_id)
);

create table platform_user
(
    id              bigint       not null primary key,
    mobile          varchar(32)  not null,
    display_name    varchar(64)  not null,
    password_hash   varchar(128) not null,
    status          varchar(32)  not null,
    token_version   int          not null default 1,
    created_at      timestamp    not null default current_timestamp,
    updated_at      timestamp    not null default current_timestamp on update current_timestamp,
    constraint uk_platform_user_mobile unique (mobile)
);

create table admin_user
(
    id              bigint       not null primary key,
    tenant_id       bigint       not null,
    store_id        bigint       not null,
    mobile          varchar(32)  not null,
    display_name    varchar(64)  not null,
    password_hash   varchar(128) not null,
    status          varchar(32)  not null,
    token_version   int          not null default 1,
    created_at      timestamp    not null default current_timestamp,
    updated_at      timestamp    not null default current_timestamp on update current_timestamp,
    constraint uk_admin_user_tenant_mobile unique (tenant_id, mobile),
    index idx_admin_user_tenant_store (tenant_id, store_id)
);

create table role
(
    id          bigint       not null primary key,
    tenant_id   bigint       null,
    domain      varchar(32)  not null,
    name        varchar(64)  not null,
    description varchar(255) null,
    system_role tinyint(1)   not null default 0,
    created_at  timestamp    not null default current_timestamp,
    updated_at  timestamp    not null default current_timestamp on update current_timestamp,
    constraint uk_role_domain_tenant_name unique (domain, tenant_id, name),
    index idx_role_tenant (tenant_id)
);

create table permission
(
    id              bigint       not null primary key,
    domain          varchar(32)  not null,
    permission_type varchar(32)  not null,
    permission_code varchar(128) not null,
    name            varchar(64)  not null,
    created_at      timestamp    not null default current_timestamp,
    constraint uk_permission_domain_code unique (domain, permission_code)
);

create table user_role
(
    id          bigint      not null primary key,
    tenant_id   bigint      null,
    domain      varchar(32) not null,
    user_id     bigint      not null,
    role_id     bigint      not null,
    created_at  timestamp   not null default current_timestamp,
    constraint uk_user_role_domain_tenant_user_role unique (domain, tenant_id, user_id, role_id),
    index idx_user_role_tenant_user (tenant_id, user_id)
);

create table role_permission
(
    id            bigint      not null primary key,
    tenant_id     bigint      null,
    domain        varchar(32) not null,
    role_id       bigint      not null,
    permission_id bigint      not null,
    created_at    timestamp   not null default current_timestamp,
    constraint uk_role_permission_domain_tenant_role_perm unique (domain, tenant_id, role_id, permission_id),
    index idx_role_permission_tenant_role (tenant_id, role_id)
);

create table operation_log
(
    id             bigint       not null primary key,
    tenant_id      bigint       null,
    store_id       bigint       null,
    domain         varchar(32)  not null,
    operator_id    bigint       not null,
    operation_type varchar(64)  not null,
    target_type    varchar(64)  not null,
    target_id      varchar(64)  null,
    detail         varchar(1024) null,
    trace_id       varchar(128) null,
    created_at     timestamp    not null default current_timestamp,
    index idx_operation_log_tenant_time (tenant_id, created_at),
    index idx_operation_log_operator_time (operator_id, created_at)
);
