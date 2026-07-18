-- 初始化平台系统预置角色及其最小职责权限。
-- 保留首席管理员原有角色主键，确保已有账号角色关联和全量授权不受影响。

update role
set name = '首席管理员', description = '平台最高权限负责人，拥有全部平台权限'
where id = 100001 and domain = 'PLATFORM' and tenant_id is null;

insert into permission (id, domain, permission_type, permission_code, name) values
(200012, 'PLATFORM', 'API', 'platform:merchant:create', '开通商户'),
(200013, 'PLATFORM', 'API', 'platform:merchant:read', '查询商户'),
(200014, 'PLATFORM', 'API', 'platform:merchant:enable', '启用商户')
on duplicate key update name = values(name);

insert into role (tenant_id, domain, name, description, status, system_role)
select null, 'PLATFORM', '平台运营', '负责商户开通、查询和启用', 'ENABLED', 1
where not exists (select 1 from role where domain = 'PLATFORM' and tenant_id is null and name = '平台运营');

insert into role (tenant_id, domain, name, description, status, system_role)
select null, 'PLATFORM', '客户顾问', '负责查询商户基础信息，为商户提供咨询服务', 'ENABLED', 1
where not exists (select 1 from role where domain = 'PLATFORM' and tenant_id is null and name = '客户顾问');

insert into role (tenant_id, domain, name, description, status, system_role)
select null, 'PLATFORM', '系统管理员', '负责平台账号、角色和权限配置', 'ENABLED', 1
where not exists (select 1 from role where domain = 'PLATFORM' and tenant_id is null and name = '系统管理员');

insert into role (tenant_id, domain, name, description, status, system_role)
select null, 'PLATFORM', '财务审计', '负责查询商户基础信息，财务明细待相关接口上线后另行授权', 'ENABLED', 1
where not exists (select 1 from role where domain = 'PLATFORM' and tenant_id is null and name = '财务审计');

-- 新增平台权限同步授予首席管理员，保持其全量平台权限。
insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', 100001, id from permission where domain = 'PLATFORM'
on duplicate key update permission_id = values(permission_id);

-- 平台运营仅可执行商户生命周期管理，不具备账号和授权管理权限。
insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null and role.name = '平台运营'
  and permission.permission_code in ('platform:merchant:create', 'platform:merchant:read', 'platform:merchant:enable')
on duplicate key update permission_id = values(permission_id);

-- 客户顾问和财务审计仅可查询商户基础信息；平台尚未提供财务明细接口。
insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null and role.name = '客户顾问'
  and permission.permission_code = 'platform:merchant:read'
on duplicate key update permission_id = values(permission_id);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null and role.name = '财务审计'
  and permission.permission_code = 'platform:merchant:read'
on duplicate key update permission_id = values(permission_id);

-- 系统管理员可管理平台账号、角色和权限配置，但不处理商户日常运营。
insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null and role.name = '系统管理员'
  and permission.permission_code in (
    'platform:account:read', 'platform:account:create', 'platform:account:update',
    'platform:account:status:update', 'platform:account:password:reset',
    'platform:role:read', 'platform:role:create', 'platform:role:update',
    'platform:role:status:update', 'platform:role:permission:assign', 'platform:permission:read'
)
on duplicate key update permission_id = values(permission_id);
