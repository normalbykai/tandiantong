create table if not exists foundation_schema_marker
(
    id          bigint       not null primary key comment '主键ID',
    marker_name varchar(64)  not null comment '基础迁移标记名称',
    created_at  timestamp    not null default current_timestamp comment '创建时间',
    constraint uk_foundation_schema_marker_name unique (marker_name)
) comment = '数据库基础版本标记表';

insert into foundation_schema_marker (id, marker_name)
values (1, 'V1 工程基础迁移')
on duplicate key update marker_name = values(marker_name);
