package com.tandiantong.security.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.security.entity.OperationLogEntity;

/**
 * 操作日志 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
