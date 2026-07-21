-- 新增平台操作日志查询权限，并授予系统管理员角色。
insert into permission (id, domain, permission_type, permission_code, name) values
(200022, 'PLATFORM', 'API', 'platform:operation-log:read', '查询平台操作日志')
on duplicate key update name = values(name);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null and role.name = '系统管理员'
  and permission.permission_code = 'platform:operation-log:read'
on duplicate key update permission_id = values(permission_id);
