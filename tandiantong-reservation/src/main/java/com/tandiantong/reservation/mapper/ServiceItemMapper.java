package com.tandiantong.reservation.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.reservation.entity.ServiceItemEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 服务项目 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ServiceItemMapper extends BaseMapper<ServiceItemEntity> {

    /**
     * 校验服务与时段是否属于同一租户门店，并返回服务支付模式。
     */
    @Select("""
            select i.payment_mode
              from service_item i
              join service_slot s on s.service_id = i.id
                   and s.tenant_id = i.tenant_id and s.store_id = i.store_id
             where i.id = #{serviceId} and s.id = #{slotId}
               and i.tenant_id = #{tenantId} and i.store_id = #{storeId}
               and i.status = #{enabledStatus}
            """)
    String selectPaymentMode(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                             @Param("serviceId") Long serviceId, @Param("slotId") Long slotId,
                             @Param("enabledStatus") String enabledStatus);
}
