-- 为平台字典项增加独立的业务实际存储值，避免编码同时承担程序标识和业务值。
alter table platform_dictionary_item
    add column item_value varchar(255) null after item_code;

update platform_dictionary_item
set item_value = item_code
where item_value is null;

alter table platform_dictionary_item
    modify column item_value varchar(255) not null comment '业务实际存储值';
