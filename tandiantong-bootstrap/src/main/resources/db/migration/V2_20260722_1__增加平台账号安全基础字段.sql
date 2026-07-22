-- 增加平台账号登录失败锁定、最后登录时间和可选密码复杂度配置。
alter table platform_user
    add column failed_login_count int not null default 0 comment '连续登录失败次数' after token_version,
    add column locked_until datetime null comment '登录锁定截止时间' after failed_login_count,
    add column last_login_at datetime null comment '最近成功登录时间' after locked_until;

alter table platform_system_config
    add column password_complexity_enabled tinyint(1) not null default 0 comment '是否启用密码复杂度校验' after fixed_reset_password_hash,
    add column password_min_length int not null default 8 comment '密码复杂度启用时的最小长度' after password_complexity_enabled;
