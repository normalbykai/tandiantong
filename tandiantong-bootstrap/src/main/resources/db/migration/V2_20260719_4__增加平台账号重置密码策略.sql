-- 增加平台账号重置密码策略；固定密码只保存 BCrypt 哈希，禁止保存明文。
alter table platform_system_config
    add column reset_password_mode varchar(16) not null default 'RANDOM' comment '账号重置密码策略' after description,
    add column fixed_reset_password_hash varchar(128) null comment '固定重置密码哈希' after reset_password_mode;

insert into permission (id, domain, permission_type, permission_code, name) values
(200027, 'PLATFORM', 'API', 'platform:system:security:read', '查询账号安全策略'),
(200028, 'PLATFORM', 'API', 'platform:system:security:update', '修改账号安全策略')
on duplicate key update permission_code = values(permission_code), name = values(name);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id
from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null
  and role.name in ('首席管理员', '系统管理员')
  and permission.permission_code in ('platform:system:security:read', 'platform:system:security:update')
on duplicate key update permission_id = values(permission_id);
