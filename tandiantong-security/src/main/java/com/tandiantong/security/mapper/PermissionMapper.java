package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.PermissionEntity;

/**
 * 权限 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface PermissionMapper extends BaseMapper<PermissionEntity> {
}
