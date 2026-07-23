-- 增加商户基础设施权限，并初始化一个仅用于本地联调的测试商户。
-- 测试商户登录：13900000001 / admin

insert into permission (id, domain, permission_type, permission_code, name) values
(200110, 'TENANT', 'VIEW', 'tenant:store:view', '查看门店信息'),
(200111, 'TENANT', 'API', 'tenant:store:update', '编辑门店信息'),
(200112, 'TENANT', 'VIEW', 'tenant:staff:view', '查看员工账号'),
(200113, 'TENANT', 'VIEW', 'tenant:role:view', '查看商户角色'),
(200114, 'TENANT', 'API', 'tenant:role:update', '维护商户角色'),
(200115, 'TENANT', 'VIEW', 'tenant:permission:view', '查看商户权限'),
(200116, 'TENANT', 'API', 'tenant:permission:read', '查询商户权限'),
(200117, 'TENANT', 'VIEW', 'tenant:operation-log:view', '查看商户操作日志'),
(200118, 'TENANT', 'API', 'tenant:operation-log:read', '查询商户操作日志'),
(200119, 'TENANT', 'VIEW', 'tenant:system:view', '查看商户系统管理'),
(200120, 'TENANT', 'API', 'tenant:system:update', '更新商户系统设置');

insert into tenant (id, tenant_code, name, status)
values (3, 'T_TEST_20260723', '湖滨小吃铺', 'ENABLED');

insert into store (id, tenant_id, name, status)
values (3, 3, '湖滨小吃铺（湖滨路店）', 'ENABLED');

insert into role (id, tenant_id, domain, role_code, name, description, status, system_role, is_authority_role)
values (100006, 3, 'TENANT', 'merchant_admin', '商户管理员', '本地联调测试商户管理员', 'ENABLED', 1, 0);

insert into admin_user (id, tenant_id, store_id, mobile, display_name, password_hash, status, token_version)
values (1784810386692, 3, 3, '13900000001', '张明',
        '$2a$10$DwEkpeFIeS0k13cVwihefO1rQo9gzFIL6eNL60m5VlaCnODW1/jH2',
        'ENABLED', 1);

insert into user_role (id, tenant_id, domain, user_id, role_id)
values (100003, 3, 'TENANT', 1784810386692, 100006);

insert into role_permission (id, tenant_id, domain, role_id, permission_id) values
(326, 3, 'TENANT', 100006, 200110),
(327, 3, 'TENANT', 100006, 200111),
(328, 3, 'TENANT', 100006, 200112),
(329, 3, 'TENANT', 100006, 200113),
(330, 3, 'TENANT', 100006, 200114),
(331, 3, 'TENANT', 100006, 200115),
(332, 3, 'TENANT', 100006, 200116),
(333, 3, 'TENANT', 100006, 200117),
(334, 3, 'TENANT', 100006, 200118),
(335, 3, 'TENANT', 100006, 200119),
(336, 3, 'TENANT', 100006, 200120);
