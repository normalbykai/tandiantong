package com.tandiantong.order.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.order.entity.SalesOrderItemEntity;

/**
 * 商品订单明细 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface SalesOrderItemMapper extends BaseMapper<SalesOrderItemEntity> {
}
