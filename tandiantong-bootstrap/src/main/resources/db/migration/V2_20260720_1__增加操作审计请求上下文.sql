-- 操作审计日志补充请求来源信息，用于权限、退款和核销等关键操作追踪。
alter table operation_log
    add column user_ip varchar(64) null comment '操作来源IP' after trace_id,
    add column user_agent varchar(512) null comment '操作来源客户端标识' after user_ip,
    add column request_method varchar(16) null comment '请求方法' after user_agent,
    add column request_url varchar(512) null comment '请求路径' after request_method;
