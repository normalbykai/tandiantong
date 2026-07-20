-- 增加平台字典类型表，存储字典类型的中文名称和描述信息，
-- 使字典类型的元数据也可通过配置展示，不再依赖前端硬编码。

create table if not exists platform_dictionary_type
(
    id              bigint       not null auto_increment primary key comment '字典类型主键',
    dictionary_type varchar(64)  not null comment '字典类型编码',
    type_label      varchar(128) not null comment '字典类型中文名称',
    description     varchar(255) null comment '字典类型描述说明',
    sort_order      int          not null default 0 comment '排序值，数字越小越靠前',
    status          varchar(32)  not null default 'ENABLED' comment '启用状态',
    created_at      timestamp    not null default current_timestamp comment '创建时间',
    updated_at      timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    constraint uk_platform_dictionary_type unique (dictionary_type)
) comment = '平台字典类型表';

-- 初始化 8 类字典类型
insert into platform_dictionary_type (dictionary_type, type_label, description, sort_order, status) values
('ENABLE_STATUS',          '通用启停状态', '跨模块复用的启用与停用状态，用于账号、角色、字典项等',         10, 'ENABLED'),
('TENANT_STATUS',          '租户状态',     '商户租户的全生命周期状态，从开通到停用',                         20, 'ENABLED'),
('ORDER_STATUS',           '订单状态',     '商品订单从创建、支付、核销到退款的全流程状态',                   30, 'ENABLED'),
('REFUND_STATUS',          '退款状态',     '订单退款的处理进度状态',                                       40, 'ENABLED'),
('PRODUCT_STATUS',         '商品状态',     '商品的上架、下架与草稿状态',                                   50, 'ENABLED'),
('RESERVATION_STATUS',     '预约状态',     '服务预约从提交、确认到履约完成的状态',                          60, 'ENABLED'),
('VERIFICATION_STATUS',    '核销状态',     '核销凭证的签发、核销与取消状态',                               70, 'ENABLED'),
('PAYMENT_CONFIG_STATUS',  '支付配置状态', '租户微信支付参数的配置与验证状态',                             80, 'ENABLED')
on duplicate key update type_label = values(type_label), description = values(description), sort_order = values(sort_order);
