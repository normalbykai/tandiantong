package com.tandiantong.reservation.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.reservation.entity.ServiceReservationEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 服务预约 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ServiceReservationMapper extends BaseMapper<ServiceReservationEntity> {

    /**
     * 按预约单号反查预约，供支付回调建立可信租户门店上下文。
     */
    @Select("""
            select * from service_reservation
             where reservation_no = #{reservationNo}
            """)
    ServiceReservationEntity selectByReservationNo(@Param("reservationNo") String reservationNo);

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
     * 查询指定时间前已过期的待支付预约，供超时取消任务逐条执行状态条件更新。
     */
    @Select("""
            select * from service_reservation
             where tenant_id = #{tenantId} and store_id = #{storeId}
               and status = #{pendingPaymentStatus} and expire_at <= #{expireBefore}
             order by expire_at, id
            """)
    List<ServiceReservationEntity> selectExpiredPendingReservations(@Param("tenantId") Long tenantId,
                                                                    @Param("storeId") Long storeId,
                                                                    @Param("pendingPaymentStatus") String pendingPaymentStatus,
                                                                    @Param("expireBefore") LocalDateTime expireBefore);

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

    /**
     * 超时任务仅允许取消待支付预约，避免已确认预约被定时任务误取消。
     */
    @Update("""
            update service_reservation set status = #{canceledStatus}
             where tenant_id = #{tenantId} and store_id = #{storeId} and reservation_no = #{reservationNo}
               and status = #{pendingPaymentStatus}
            """)
    int cancelPendingPayment(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                             @Param("reservationNo") String reservationNo,
                             @Param("pendingPaymentStatus") String pendingPaymentStatus,
                             @Param("canceledStatus") String canceledStatus);

    /**
     * 支付成功后仅允许待支付预约原子确认，并保存微信交易流水。
     */
    @Update("""
            update service_reservation
               set status = #{confirmedStatus}, transaction_id = #{transactionId}
             where tenant_id = #{tenantId} and store_id = #{storeId}
               and reservation_no = #{reservationNo} and status = #{pendingPaymentStatus}
            """)
    int confirmPayment(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                       @Param("reservationNo") String reservationNo,
                       @Param("pendingPaymentStatus") String pendingPaymentStatus,
                       @Param("confirmedStatus") String confirmedStatus,
                       @Param("transactionId") String transactionId);

    /**
     * 首次确认后写入预约核销凭证信息，重复回调不会覆盖已有凭证。
     */
    @Update("""
            update service_reservation
               set voucher_code = #{voucherCode}
             where tenant_id = #{tenantId} and store_id = #{storeId}
               and reservation_no = #{reservationNo} and voucher_code is null
            """)
    int attachVoucher(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                      @Param("reservationNo") String reservationNo,
                      @Param("voucherCode") String voucherCode);

    /**
     * 核销成功后仅允许已确认预约推进为已履约，保证预约状态不能越级流转。
     */
    @Update("""
            update service_reservation set status = #{fulfilledStatus}
             where tenant_id = #{tenantId} and store_id = #{storeId} and reservation_no = #{reservationNo}
               and status = #{confirmedStatus}
            """)
    int fulfillAfterVerification(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                                 @Param("reservationNo") String reservationNo,
                                 @Param("confirmedStatus") String confirmedStatus,
                                 @Param("fulfilledStatus") String fulfilledStatus);
}
