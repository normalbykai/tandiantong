package com.tandiantong.order.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.order.entity.BusinessIdempotencyRecordEntity;

/**
 * 业务幂等记录 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface BusinessIdempotencyRecordMapper extends BaseMapper<BusinessIdempotencyRecordEntity> {
}
