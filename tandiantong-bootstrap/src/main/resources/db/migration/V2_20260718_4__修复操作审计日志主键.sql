-- 操作审计日志由 MyBatis-Plus 使用数据库自增主键，避免关键管理操作因日志写入失败而整体回滚。
alter table operation_log modify id bigint not null auto_increment comment '操作日志ID';
