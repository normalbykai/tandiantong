-- 系统管理员不负责商户日常运营，移除其租户管理菜单视图权限。
delete role_permission
from role_permission
join role on role.id = role_permission.role_id
join permission on permission.id = role_permission.permission_id
where role.domain = 'PLATFORM'
  and role.tenant_id is null
  and role.role_code = 'platfrom_system_admin'
  and permission.domain = 'PLATFORM'
  and permission.permission_code = 'platform:merchant:view';
