package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.AdminUserEntity;

/**
 * 租户后台用户 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface AdminUserMapper extends BaseMapper<AdminUserEntity> {
}
