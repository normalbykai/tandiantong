-- 受保护管理角色可稳定维护角色与权限，不会因修改普通权限而失去恢复配置的能力。
alter table role add column is_authority_role tinyint not null default 0 comment '是否受保护管理角色' after system_role;

update role set is_authority_role = 1
where domain = 'PLATFORM' and tenant_id is null
  and role_code in ('platfrom_super_admin', 'platfrom_system_admin');
