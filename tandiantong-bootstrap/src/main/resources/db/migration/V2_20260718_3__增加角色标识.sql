-- 角色标识用于程序稳定识别；平台和商户分别采用独立前缀。
alter table role add column role_code varchar(64) null after domain;

update role set role_code = case
    when domain = 'PLATFORM' and id = 100001 then 'platfrom_super_admin'
    when domain = 'PLATFORM' and name = '平台运营' then 'platfrom_operations'
    when domain = 'PLATFORM' and name = '客户顾问' then 'platfrom_customer_advisor'
    when domain = 'PLATFORM' and name = '系统管理员' then 'platfrom_system_admin'
    when domain = 'PLATFORM' and name = '财务审计' then 'platfrom_finance_auditor'
    when domain = 'PLATFORM' then concat('platfrom_role_', id)
    else concat('merchant_role_', id)
end
where role_code is null or role_code = '';

alter table role modify role_code varchar(64) not null comment '角色标识';
alter table role add constraint uk_role_domain_tenant_code unique (domain, tenant_id, role_code);
