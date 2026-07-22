-- 为平台管理端增加菜单视图权限，已有 API 权限保持不变。
insert into permission (id, domain, permission_type, permission_code, name) values
(200101, 'PLATFORM', 'VIEW', 'platform:merchant:view', '查看租户管理'),
(200102, 'PLATFORM', 'VIEW', 'platform:operation-log:view', '查看操作日志菜单'),
(200103, 'PLATFORM', 'VIEW', 'platform:system:config:view', '查看系统配置菜单'),
(200104, 'PLATFORM', 'VIEW', 'platform:system:security:view', '查看安全设置菜单'),
(200105, 'PLATFORM', 'VIEW', 'platform:system:dictionary:view', '查看平台字典菜单'),
(200106, 'PLATFORM', 'VIEW', 'platform:access:account:view', '查看平台账号菜单'),
(200107, 'PLATFORM', 'VIEW', 'platform:access:role:view', '查看平台角色菜单'),
(200108, 'PLATFORM', 'VIEW', 'platform:access:permission:view', '查看平台权限菜单');

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id
from role
join permission
  on permission.domain = 'PLATFORM'
 and permission.permission_type = 'VIEW'
where role.domain = 'PLATFORM'
  and role.tenant_id is null
  and role.role_code = 'platfrom_super_admin';

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id
from role
join permission
  on permission.domain = 'PLATFORM'
 and permission.permission_type = 'VIEW'
 and permission.permission_code in (
     'platform:merchant:view',
     'platform:operation-log:view',
     'platform:system:config:view',
     'platform:system:security:view',
     'platform:system:dictionary:view',
     'platform:access:account:view',
     'platform:access:role:view',
     'platform:access:permission:view'
 )
where role.domain = 'PLATFORM'
  and role.tenant_id is null
  and role.role_code = 'platfrom_system_admin';

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id
from role
join permission
  on permission.domain = 'PLATFORM'
 and permission.permission_type = 'VIEW'
 and permission.permission_code in ('platform:dashboard:view', 'platform:merchant:view')
where role.domain = 'PLATFORM'
  and role.tenant_id is null
  and role.role_code in ('platfrom_operations', 'platfrom_customer_advisor');
