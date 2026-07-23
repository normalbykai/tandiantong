package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.MerchantSystemConfigEntity;

/** 商户展示配置 Mapper。 */
@InterceptorIgnore(tenantLine = "true")
public interface MerchantSystemConfigMapper extends BaseMapper<MerchantSystemConfigEntity> {
}
