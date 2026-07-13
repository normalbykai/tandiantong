package com.tandiantong.reservation.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.reservation.entity.ServiceSlotEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 服务时段 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ServiceSlotMapper extends BaseMapper<ServiceSlotEntity> {

    /**
     * 查询指定租户门店可预约的服务时段。
     */
    @Select("""
            select i.id service_id, i.name service_name, i.payment_mode payment_mode,
                   i.price_cent price_cent, i.duration_minutes duration_minutes,
                   s.id slot_id, s.service_date service_date, s.start_time start_time,
                   s.end_time end_time, s.capacity capacity, s.used_capacity used_capacity
              from service_item i
              join service_slot s on s.service_id = i.id
                   and s.tenant_id = i.tenant_id and s.store_id = i.store_id
             where i.tenant_id = #{tenantId} and i.store_id = #{storeId}
               and i.status = #{enabledStatus} and s.paused = false
               and s.service_date >= #{today}
             order by s.service_date, s.start_time
            """)
    List<Map<String, Object>> selectAvailableServices(@Param("tenantId") Long tenantId,
                                                       @Param("storeId") Long storeId,
                                                       @Param("enabledStatus") String enabledStatus,
                                                       @Param("today") LocalDate today);

    /**
     * 原子占用一个预约容量，防止并发超额预约。
     */
    @Update("""
            update service_slot
               set used_capacity = used_capacity + 1, version = version + 1
             where id = #{slotId} and tenant_id = #{tenantId} and store_id = #{storeId}
               and paused = false and used_capacity < capacity
            """)
    int occupyCapacity(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                       @Param("slotId") Long slotId);

    /**
     * 取消预约后原子释放一个已占用容量。
     */
    @Update("""
            update service_slot
               set used_capacity = used_capacity - 1, version = version + 1
             where id = #{slotId} and tenant_id = #{tenantId} and store_id = #{storeId}
               and used_capacity > 0
            """)
    int releaseCapacity(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                        @Param("slotId") Long slotId);
}
