package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.StoreEntity;

/**
 * 门店 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface StoreMapper extends BaseMapper<StoreEntity> {
}
