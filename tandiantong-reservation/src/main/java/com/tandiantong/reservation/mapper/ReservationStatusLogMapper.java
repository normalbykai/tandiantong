package com.tandiantong.reservation.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.reservation.entity.ReservationStatusLogEntity;

/**
 * 预约状态日志 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ReservationStatusLogMapper extends BaseMapper<ReservationStatusLogEntity> {
}
