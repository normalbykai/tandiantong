-- 初始化平台超级管理员账号和角色。
-- 密码 admin 使用 PasswordService 生成 BCrypt 哈希后入库。

insert into platform_user (id, mobile, display_name, password_hash, status, token_version)
values (100001, 'admin', '平台超级管理员', '$2a$10$DwEkpeFIeS0k13cVwihefO1rQo9gzFIL6eNL60m5VlaCnODW1/jH2', 'ENABLED', 1)
on duplicate key update
    mobile = values(mobile),
    display_name = values(display_name),
    password_hash = values(password_hash),
    status = values(status),
    token_version = values(token_version);

insert into role (id, tenant_id, domain, name, description, system_role)
values (100001, null, 'PLATFORM', 'super_admin', '平台超级管理员', 1)
on duplicate key update
    tenant_id = values(tenant_id),
    domain = values(domain),
    name = values(name),
    description = values(description),
    system_role = values(system_role);

insert into user_role (id, tenant_id, domain, user_id, role_id)
values (100001, null, 'PLATFORM', 100001, 100001)
on duplicate key update
    tenant_id = values(tenant_id),
    domain = values(domain),
    user_id = values(user_id),
    role_id = values(role_id);
