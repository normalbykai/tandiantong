-- 增加可动态调整的登录锁定和密码复杂度规则，默认保持当前安全策略。
alter table platform_system_config
    add column login_lock_enabled tinyint(1) not null default 1 comment '是否启用登录失败锁定' after password_min_length,
    add column login_failure_threshold int not null default 5 comment '触发登录锁定的连续失败次数' after login_lock_enabled,
    add column login_lock_minutes int not null default 15 comment '登录锁定时长，单位分钟' after login_failure_threshold,
    add column require_uppercase tinyint(1) not null default 1 comment '密码是否要求大写字母' after login_lock_minutes,
    add column require_lowercase tinyint(1) not null default 1 comment '密码是否要求小写字母' after require_uppercase,
    add column require_digit tinyint(1) not null default 1 comment '密码是否要求数字' after require_lowercase,
    add column require_special_character tinyint(1) not null default 0 comment '密码是否要求特殊字符' after require_digit;
