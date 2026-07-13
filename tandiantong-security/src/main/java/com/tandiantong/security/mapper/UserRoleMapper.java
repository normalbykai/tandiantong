package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.UserRoleEntity;

/**
 * 用户角色关系 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {
}
