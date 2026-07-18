-- 平台账号、角色与权限管理的主键和状态约束。
alter table platform_user modify id bigint not null auto_increment;
alter table role modify id bigint not null auto_increment;
alter table permission modify id bigint not null auto_increment;
alter table user_role modify id bigint not null auto_increment;
alter table role_permission modify id bigint not null auto_increment;

alter table role add column status varchar(32) not null default 'ENABLED' after description;

insert into permission (id, domain, permission_type, permission_code, name) values
(200001, 'PLATFORM', 'API', 'platform:account:read', '查询平台账号'),
(200002, 'PLATFORM', 'API', 'platform:account:create', '新增平台账号'),
(200003, 'PLATFORM', 'API', 'platform:account:update', '修改平台账号'),
(200004, 'PLATFORM', 'API', 'platform:account:status:update', '启停平台账号'),
(200005, 'PLATFORM', 'API', 'platform:account:password:reset', '重置平台账号密码'),
(200006, 'PLATFORM', 'API', 'platform:role:read', '查询平台角色'),
(200007, 'PLATFORM', 'API', 'platform:role:create', '新增平台角色'),
(200008, 'PLATFORM', 'API', 'platform:role:update', '修改平台角色'),
(200009, 'PLATFORM', 'API', 'platform:role:status:update', '启停平台角色'),
(200010, 'PLATFORM', 'API', 'platform:role:permission:assign', '配置平台角色权限'),
(200011, 'PLATFORM', 'API', 'platform:permission:read', '查询平台权限点')
on duplicate key update name = values(name);

insert into role_permission (tenant_id, domain, role_id, permission_id)
select null, 'PLATFORM', 100001, id from permission where domain = 'PLATFORM'
on duplicate key update permission_id = values(permission_id);
