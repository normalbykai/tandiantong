package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.RoleEntity;

/**
 * 角色 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface RoleMapper extends BaseMapper<RoleEntity> {
}
