-- 修正平台操作日志权限编号，避免复用已存在的 200022 导致系统配置权限被覆盖。
-- 旧迁移 V2_20260721_2 已经执行过的环境会保留错误结果，因此这里通过新迁移统一修正。

update permission
set permission_code = 'platform:system:config:update',
    name = '修改平台系统配置'
where id = 200022
  and domain = 'PLATFORM'
  and permission_type = 'API';

insert into permission (id, domain, permission_type, permission_code, name) values
(200029, 'PLATFORM', 'API', 'platform:operation-log:read', '查询平台操作日志')
on duplicate key update permission_code = values(permission_code),
                        name = values(name);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', role.id, permission.id from role join permission
where role.domain = 'PLATFORM' and role.tenant_id is null
  and role.name in ('首席管理员', '系统管理员')
  and permission.permission_code = 'platform:operation-log:read'
on duplicate key update permission_id = values(permission_id);
