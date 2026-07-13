package com.tandiantong.order.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.order.entity.OrderStatusLogEntity;

/**
 * 订单状态日志 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface OrderStatusLogMapper extends BaseMapper<OrderStatusLogEntity> {
}
