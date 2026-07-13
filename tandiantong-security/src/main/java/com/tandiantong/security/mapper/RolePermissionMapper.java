package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.RolePermissionEntity;

/**
 * 角色权限关系 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface RolePermissionMapper extends BaseMapper<RolePermissionEntity> {
}
