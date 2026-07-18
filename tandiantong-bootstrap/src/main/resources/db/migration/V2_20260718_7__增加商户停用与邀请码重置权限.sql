insert into permission (id, domain, permission_type, permission_code, name) values
(200015, 'PLATFORM', 'API', 'platform:merchant:disable', '停用商户'),
(200016, 'PLATFORM', 'API', 'platform:merchant:invitation:reissue', '重新生成商户邀请码')
on duplicate key update name = values(name);

-- 首席管理员保持全部平台权限；平台运营负责商户生命周期和待激活管理员邀请处理。
insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id
from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null
  and role.role_code in ('platfrom_super_admin', 'platfrom_system_admin')
  and permission.permission_code in ('platform:merchant:disable', 'platform:merchant:invitation:reissue')
on duplicate key update permission_id = values(permission_id);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id
from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null and role.name = '平台运营'
  and permission.permission_code in ('platform:merchant:disable', 'platform:merchant:invitation:reissue')
on duplicate key update permission_id = values(permission_id);
