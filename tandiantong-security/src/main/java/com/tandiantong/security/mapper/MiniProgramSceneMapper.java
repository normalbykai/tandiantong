package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.MiniProgramSceneEntity;

/**
 * 小程序入口码 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface MiniProgramSceneMapper extends BaseMapper<MiniProgramSceneEntity> {
}
