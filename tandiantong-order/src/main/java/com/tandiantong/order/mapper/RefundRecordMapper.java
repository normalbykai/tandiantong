package com.tandiantong.order.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.order.entity.RefundRecordEntity;

/**
 * 退款记录 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface RefundRecordMapper extends BaseMapper<RefundRecordEntity> {
}
