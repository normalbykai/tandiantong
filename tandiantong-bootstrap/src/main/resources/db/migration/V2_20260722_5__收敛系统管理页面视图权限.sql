-- 系统配置、字典管理和安全设置属于系统管理页面内部功能分段，不单独作为左侧菜单授权。
insert into permission (id, domain, permission_type, permission_code, name)
values (200109, 'PLATFORM', 'VIEW', 'platform:system:view', '查看系统管理')
;

-- 原错误分段权限已经授予的角色统一迁移到系统管理页面权限，避免角色失去原有入口。
insert into role_permission (tenant_id, domain, role_id, permission_id)
select distinct rp.tenant_id, rp.domain, rp.role_id, 200109
from role_permission rp
join permission p on p.id = rp.permission_id
where rp.domain = 'PLATFORM'
  and p.domain = 'PLATFORM'
  and p.permission_type = 'VIEW'
  and p.permission_code in (
      'platform:system:config:view',
      'platform:system:security:view',
      'platform:system:dictionary:view'
  );

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, 200109
from role
where role.domain = 'PLATFORM'
  and role.tenant_id is null
  and role.role_code in ('platfrom_super_admin', 'platfrom_system_admin')
  and not exists (
      select 1
      from role_permission existing_relation
      where existing_relation.domain = 'PLATFORM'
        and existing_relation.tenant_id is null
        and existing_relation.role_id = role.id
        and existing_relation.permission_id = 200109
  );

delete role_permission
from role_permission
join permission p on p.id = role_permission.permission_id
where role_permission.domain = 'PLATFORM'
  and p.domain = 'PLATFORM'
  and p.permission_type = 'VIEW'
  and p.permission_code in (
      'platform:system:config:view',
      'platform:system:security:view',
      'platform:system:dictionary:view'
  );

delete from permission
where domain = 'PLATFORM'
  and permission_type = 'VIEW'
  and permission_code in (
      'platform:system:config:view',
      'platform:system:security:view',
      'platform:system:dictionary:view'
  );
