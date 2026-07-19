-- 增加平台系统配置和平台通用字典，数据不属于任何商户租户。

create table if not exists platform_system_config
(
    id          bigint       not null primary key comment '固定配置主键',
    logo_url    varchar(1024) not null comment '平台 Logo 图片地址',
    description varchar(255) not null comment '平台描述信息',
    updated_at  timestamp    not null default current_timestamp on update current_timestamp comment '最近修改时间',
    updated_by  bigint       null comment '最近修改人平台账号ID'
) comment = '平台系统配置表';

insert into platform_system_config (id, logo_url, description)
values (1, '/assets/tandiantong-logo-mark-v4.svg', '面向线下商户的经营管理平台')
on duplicate key update id = values(id);

create table if not exists platform_dictionary_item
(
    id              bigint       not null auto_increment primary key comment '字典项主键',
    dictionary_type varchar(64)  not null comment '字典类型编码',
    item_code       varchar(64)  not null comment '字典项编码',
    item_label      varchar(128) not null comment '字典项名称',
    sort_order      int          not null default 0 comment '排序值',
    status          varchar(32)  not null default 'ENABLED' comment '启用状态',
    created_at      timestamp    not null default current_timestamp comment '创建时间',
    updated_at      timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_platform_dictionary_type_code unique (dictionary_type, item_code),
    index idx_platform_dictionary_type_status (dictionary_type, status)
) comment = '平台通用字典项表';

insert into permission (id, domain, permission_type, permission_code, name) values
(200015, 'PLATFORM', 'API', 'platform:system:config:read', '查询平台系统配置'),
(200016, 'PLATFORM', 'API', 'platform:system:config:update', '修改平台系统配置'),
(200017, 'PLATFORM', 'API', 'platform:dictionary:read', '查询平台字典'),
(200018, 'PLATFORM', 'API', 'platform:dictionary:create', '新增平台字典项'),
(200019, 'PLATFORM', 'API', 'platform:dictionary:update', '编辑平台字典项'),
(200020, 'PLATFORM', 'API', 'platform:dictionary:status:update', '启停平台字典项')
on duplicate key update name = values(name);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', 100001, id from permission
where domain = 'PLATFORM' and permission_code like 'platform:system:%'
   or domain = 'PLATFORM' and permission_code like 'platform:dictionary:%'
on duplicate key update permission_id = values(permission_id);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null and role.name = '系统管理员'
  and permission.permission_code in (
    'platform:system:config:read', 'platform:system:config:update',
    'platform:dictionary:read', 'platform:dictionary:create',
    'platform:dictionary:update', 'platform:dictionary:status:update'
  )
on duplicate key update permission_id = values(permission_id);
