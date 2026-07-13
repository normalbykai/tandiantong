package com.tandiantong.order.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.order.entity.SalesOrderEntity;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品订单 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface SalesOrderMapper extends BaseMapper<SalesOrderEntity> {

    /**
     * 按租户、门店和订单号加行锁查询订单。
     */
    @Select("""
            select * from sales_order
             where tenant_id = #{tenantId} and store_id = #{storeId} and order_no = #{orderNo}
             for update
            """)
    SalesOrderEntity selectForUpdate(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                                     @Param("orderNo") String orderNo);

    /**
     * 支付回调按全局唯一订单号反查订单，不依赖登录租户上下文。
     */
    @Select("select * from sales_order where order_no = #{orderNo}")
    SalesOrderEntity selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 按当前状态原子推进订单状态并记录支付时间。
     */
    @Update("""
            update sales_order set status = #{targetStatus}, paid_at = #{paidAt}
             where tenant_id = #{tenantId} and order_no = #{orderNo} and status = #{currentStatus}
            """)
    int updatePaidStatus(@Param("tenantId") Long tenantId, @Param("orderNo") String orderNo,
                         @Param("currentStatus") String currentStatus, @Param("targetStatus") String targetStatus,
                         @Param("paidAt") LocalDateTime paidAt);

    /**
     * 按租户、门店和当前状态原子推进订单状态。
     */
    @Update("""
            update sales_order set status = #{targetStatus}
             where tenant_id = #{tenantId} and store_id = #{storeId} and order_no = #{orderNo}
               and status = #{currentStatus}
            """)
    int updateStatus(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                     @Param("orderNo") String orderNo, @Param("currentStatus") String currentStatus,
                     @Param("targetStatus") String targetStatus);
}
