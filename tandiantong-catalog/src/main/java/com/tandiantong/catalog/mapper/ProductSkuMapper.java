package com.tandiantong.catalog.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.catalog.entity.ProductSkuEntity;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 SKU Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ProductSkuMapper extends BaseMapper<ProductSkuEntity> {

    /**
     * 查询可下单 SKU 及商品快照信息。
     */
    @Select("""
            select s.id sku_id, p.id product_id, p.name product_name, s.specification_text specification_text,
                   s.price_cent price_cent
              from product_sku s
              join product p on p.id = s.product_id
                   and p.tenant_id = s.tenant_id and p.store_id = s.store_id
             where s.id = #{skuId} and s.tenant_id = #{tenantId} and s.store_id = #{storeId}
               and s.enabled = true and p.status = 'ON_SHELF'
            """)
    Map<String, Object> selectOrderableSku(@Param("tenantId") Long tenantId,
                                            @Param("storeId") Long storeId,
                                            @Param("skuId") Long skuId);

    /**
     * 原子锁定可售库存，避免并发下单超卖。
     */
    @Update("""
            update product_sku
               set available_stock = available_stock - #{quantity},
                   locked_stock = locked_stock + #{quantity}
             where id = #{skuId} and tenant_id = #{tenantId} and store_id = #{storeId}
               and available_stock >= #{quantity}
            """)
    int lockStock(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                  @Param("skuId") Long skuId, @Param("quantity") int quantity);

    /**
     * 支付成功后原子扣减锁定库存。
     */
    @Update("""
            update product_sku
               set locked_stock = locked_stock - #{quantity}
             where id = #{skuId} and tenant_id = #{tenantId} and store_id = #{storeId}
               and locked_stock >= #{quantity}
            """)
    int confirmLockedStock(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                           @Param("skuId") Long skuId, @Param("quantity") int quantity);

    /**
     * 退款成功后回补可售库存。
     */
    @Update("""
            update product_sku
               set available_stock = available_stock + #{quantity}
             where id = #{skuId} and tenant_id = #{tenantId} and store_id = #{storeId}
            """)
    int restoreAvailableStock(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId,
                              @Param("skuId") Long skuId, @Param("quantity") int quantity);
}
