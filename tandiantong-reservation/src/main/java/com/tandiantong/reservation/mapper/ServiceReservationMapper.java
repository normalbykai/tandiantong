package com.tandiantong.reservation.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.reservation.entity.ServiceReservationEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 服务预约 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ServiceReservationMapper extends BaseMapper<ServiceReservationEntity> {

    /**
     * 按租户、门店和预约单号加行锁查询预约。
     */
    @Select("""
            select * from service_reservation
             where tenant_id = #{tenantId} and store_id = #{storeId} and reservation_no = #{reservationNo}
             for update
            """)
    ServiceReservationEntity selectForUpdate(@Param("tenantId") Long tenantId,
                                             @Param("storeId") Long storeId,
                                             @Param("reservationNo") String reservationNo);

    /**
     * 仅允许从指定当前状态集合原子取消预约。
     */
    @Update("""
            update service_reservation set status = #{canceledStatus}
             where tenant_id = #{tenantId} and store_id = #{storeId} and reservation_no = #{reservationNo}
               and status in (#{confirmedStatus}, #{pendingPaymentStatus})
            """)
    int cancel(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
               @Param("reservationNo") String reservationNo, @Param("canceledStatus") String canceledStatus,
               @Param("confirmedStatus") String confirmedStatus,
               @Param("pendingPaymentStatus") String pendingPaymentStatus);
}
