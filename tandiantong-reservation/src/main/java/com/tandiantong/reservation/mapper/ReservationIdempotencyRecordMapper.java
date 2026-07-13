package com.tandiantong.reservation.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.reservation.entity.ReservationIdempotencyRecordEntity;

/**
 * 预约业务幂等记录 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ReservationIdempotencyRecordMapper extends BaseMapper<ReservationIdempotencyRecordEntity> {
}
