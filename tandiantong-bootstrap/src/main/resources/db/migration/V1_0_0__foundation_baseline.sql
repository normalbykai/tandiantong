create table if not exists foundation_schema_marker
(
    id          bigint       not null primary key,
    marker_name varchar(64)  not null,
    created_at  timestamp    not null default current_timestamp,
    constraint uk_foundation_schema_marker_name unique (marker_name)
);

insert into foundation_schema_marker (id, marker_name)
values (1, 'V1 工程基础迁移')
on duplicate key update marker_name = values(marker_name);
