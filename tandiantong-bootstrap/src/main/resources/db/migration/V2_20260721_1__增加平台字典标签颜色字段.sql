-- 为平台字典项增加标签颜色类型，并移除重复的业务实际存储值字段。

alter table platform_dictionary_item
    add column tag_type varchar(16) not null default '' comment '字典项标签颜色类型' after item_label;

update platform_dictionary_item
set tag_type = case
                   when item_code in ('ENABLED', 'SUCCESS', 'VERIFIED', 'COMPLETED', 'FULFILLED', 'ON_SHELF')
                       then 'success'
                   when item_code = 'DISABLED'
                       then 'info'
                   when item_code in ('FAILED', 'VERIFY_FAILED', 'CANCELED')
                       then 'danger'
                   when item_code in ('PROCESSING', 'REFUNDING', 'PENDING_PAYMENT', 'PENDING_VERIFY', 'PENDING_REVIEW', 'PENDING_ENABLE', 'PENDING')
                       then 'warning'
                   else 'info'
    end;

alter table platform_dictionary_item
    drop column item_value;
