package com.tandiantong.catalog.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.catalog.entity.InventoryRecordEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存流水 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface InventoryRecordMapper extends BaseMapper<InventoryRecordEntity> {

    /**
     * 查询库存流水展示数据。
     */
    @Select("""
            select r.id id,
                   r.created_at created_at,
                   r.change_type change_type,
                   p.name product_name,
                   s.specification_text specification_text,
                   r.quantity quantity,
                   r.business_no business_no,
                   r.reason reason
              from inventory_record r
              join product_sku s on s.id = r.sku_id
                   and s.tenant_id = r.tenant_id and s.store_id = r.store_id
              join product p on p.id = s.product_id
                   and p.tenant_id = s.tenant_id and p.store_id = s.store_id
             where r.tenant_id = #{tenantId}
               and r.store_id = #{storeId}
             order by r.id desc
             limit 500
            """)
    List<Map<String, Object>> selectInventoryRecordViews(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId);
}
