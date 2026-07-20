-- 系统超级管理员身份独立于可配置角色权限，防止其调整自身角色后失去恢复权限的能力。
alter table platform_user add column is_system_super_admin tinyint not null default 0 comment '是否系统超级管理员' after token_version;

update platform_user set is_system_super_admin = 1 where id = 100001;
