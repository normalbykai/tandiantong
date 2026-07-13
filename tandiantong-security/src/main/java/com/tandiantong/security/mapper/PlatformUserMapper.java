package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.PlatformUserEntity;

/**
 * 平台用户 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface PlatformUserMapper extends BaseMapper<PlatformUserEntity> {
}
