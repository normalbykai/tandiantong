package com.tandiantong.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.analytics.entity.AnalyticsOrderFactEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单经营事实 Mapper。
 */
public interface AnalyticsOrderFactMapper extends BaseMapper<AnalyticsOrderFactEntity> {

    /**
     * 按租户、门店和日期范围汇总订单经营指标。
     */
    @Select("""
            select count(*) order_count,
                   coalesce(sum(case when status in ('PENDING_VERIFY', 'COMPLETED', 'REFUNDED')
                                then pay_amount_cent else 0 end), 0) gross_cent,
                   coalesce(sum(case when status = 'REFUNDED' then pay_amount_cent else 0 end), 0) refund_cent,
                   coalesce(sum(case when status = 'PENDING_VERIFY' then 1 else 0 end), 0) pending_verification
              from sales_order
             where tenant_id = #{tenantId} and store_id = #{storeId}
               and date(created_at) between #{startDate} and #{endDate}
            """)
    Map<String, Object> selectOrderSummary(@Param("tenantId") Long tenantId,
                                            @Param("storeId") Long storeId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * 查询销量最高的二十个商品成交快照。
     */
    @Select("""
            select i.product_name product_name, sum(i.quantity) quantity,
                   sum(i.subtotal_cent) amount_cent
              from sales_order_item i
              join sales_order o on o.tenant_id = i.tenant_id and o.store_id = i.store_id
                   and o.order_no = i.order_no
             where i.tenant_id = #{tenantId} and i.store_id = #{storeId}
               and date(i.created_at) between #{startDate} and #{endDate}
               and o.status in ('PENDING_VERIFY', 'COMPLETED', 'REFUNDED')
             group by i.product_name
             order by quantity desc
             limit 20
            """)
    List<Map<String, Object>> selectProductRanking(@Param("tenantId") Long tenantId,
                                                    @Param("storeId") Long storeId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}
