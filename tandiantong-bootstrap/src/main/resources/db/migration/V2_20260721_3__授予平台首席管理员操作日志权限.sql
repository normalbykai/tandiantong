-- 为平台首席管理员补授平台操作日志查询权限，避免修改已执行迁移脚本。
insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', 100001, id from permission
where domain = 'PLATFORM' and permission_code = 'platform:operation-log:read'
on duplicate key update permission_id = values(permission_id);
