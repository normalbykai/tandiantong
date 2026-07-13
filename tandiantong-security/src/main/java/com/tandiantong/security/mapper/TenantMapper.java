package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.TenantEntity;

/**
 * 租户主表 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface TenantMapper extends BaseMapper<TenantEntity> {
}
