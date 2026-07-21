-- 租户实体使用 PENDING_ENABLE，字典编码必须与业务状态值一致。
update platform_dictionary_item
set item_code = 'PENDING_ENABLE'
where dictionary_type = 'TENANT_STATUS'
  and item_code = 'PENDING';
