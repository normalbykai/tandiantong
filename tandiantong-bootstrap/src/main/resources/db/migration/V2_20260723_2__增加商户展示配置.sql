-- 为每个商户增加独立的展示配置，禁止读取平台系统配置。
create table merchant_system_config
(
    id          bigint       not null primary key comment '商户系统配置ID',
    tenant_id   bigint       not null comment '租户ID',
    store_id    bigint       not null comment '门店ID',
    short_name  varchar(64)  not null comment '门店简称',
    notice      varchar(255) not null comment '门店公告',
    created_at  timestamp    not null default current_timestamp comment '创建时间',
    updated_at  timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_merchant_system_config_store unique (tenant_id, store_id),
    index idx_merchant_system_config_tenant (tenant_id, store_id)
) comment = '商户管理端独立展示配置表';

insert into merchant_system_config (id, tenant_id, store_id, short_name, notice)
values (1, 3, 3, '湖滨小吃铺', '欢迎光临，营业时间以门店现场安排为准。');

insert into permission (id, domain, permission_type, permission_code, name)
values (200121, 'TENANT', 'API', 'tenant:staff:update', '维护员工账号');

insert into role_permission (id, tenant_id, domain, role_id, permission_id)
values (337, 3, 'TENANT', 100006, 200121);
