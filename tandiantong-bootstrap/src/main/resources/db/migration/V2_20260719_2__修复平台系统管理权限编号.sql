-- 修复平台系统管理权限与既有商户权限主键冲突的问题。
-- V2_20260718_7 已使用 200015、200016，平台系统权限从 200021 开始。

update permission
set name = '停用商户'
where id = 200015 and domain = 'PLATFORM' and permission_code = 'platform:merchant:disable';

update permission
set name = '重新生成商户邀请码'
where id = 200016 and domain = 'PLATFORM' and permission_code = 'platform:merchant:invitation:reissue';

insert into permission (id, domain, permission_type, permission_code, name) values
(200021, 'PLATFORM', 'API', 'platform:system:config:read', '查询平台系统配置'),
(200022, 'PLATFORM', 'API', 'platform:system:config:update', '修改平台系统配置'),
(200023, 'PLATFORM', 'API', 'platform:dictionary:read', '查询平台字典'),
(200024, 'PLATFORM', 'API', 'platform:dictionary:create', '新增平台字典项'),
(200025, 'PLATFORM', 'API', 'platform:dictionary:update', '编辑平台字典项'),
(200026, 'PLATFORM', 'API', 'platform:dictionary:status:update', '启停平台字典项')
on duplicate key update permission_code = values(permission_code), name = values(name);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null
  and role.name in ('首席管理员', '系统管理员')
  and permission.permission_code in (
    'platform:system:config:read', 'platform:system:config:update',
    'platform:dictionary:read', 'platform:dictionary:create',
    'platform:dictionary:update', 'platform:dictionary:status:update'
  )
on duplicate key update permission_id = values(permission_id);
