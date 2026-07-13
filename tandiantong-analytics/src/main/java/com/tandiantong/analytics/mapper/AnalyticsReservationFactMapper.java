package com.tandiantong.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.analytics.entity.AnalyticsReservationFactEntity;
import java.time.LocalDate;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 预约经营事实 Mapper。
 */
public interface AnalyticsReservationFactMapper extends BaseMapper<AnalyticsReservationFactEntity> {

    /**
     * 按租户、门店和日期范围汇总预约经营指标。
     */
    @Select("""
            select count(*) total,
                   coalesce(sum(case when status = 'CANCELED' then 1 else 0 end), 0) canceled,
                   coalesce(sum(case when status = 'FULFILLED' then 1 else 0 end), 0) fulfilled
              from service_reservation
             where tenant_id = #{tenantId} and store_id = #{storeId}
               and date(created_at) between #{startDate} and #{endDate}
            """)
    Map<String, Object> selectReservationSummary(@Param("tenantId") Long tenantId,
                                                  @Param("storeId") Long storeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
