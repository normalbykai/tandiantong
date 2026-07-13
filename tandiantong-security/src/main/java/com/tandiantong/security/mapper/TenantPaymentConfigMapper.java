package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.TenantPaymentConfigEntity;

/**
 * 租户支付配置 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface TenantPaymentConfigMapper extends BaseMapper<TenantPaymentConfigEntity> {
}
