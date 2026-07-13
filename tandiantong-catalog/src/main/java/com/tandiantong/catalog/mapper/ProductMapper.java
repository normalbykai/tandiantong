package com.tandiantong.catalog.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.catalog.entity.ProductEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ProductMapper extends BaseMapper<ProductEntity> {

    /**
     * 按小程序入口码查询上架商品和默认 SKU。
     */
    @Select("""
            select p.id product_id,
                   p.name product_name,
                   p.description description,
                   coalesce(c.name, '推荐') category_name,
                   s.id sku_id,
                   s.price_cent price_cent,
                   s.available_stock available_stock
              from mini_program_scene m
              join product p on p.tenant_id = m.tenant_id and p.store_id = m.store_id
              left join product_category c on c.id = p.category_id
                   and c.tenant_id = p.tenant_id and c.store_id = p.store_id
              join product_sku s on s.product_id = p.id
                   and s.tenant_id = p.tenant_id and s.store_id = p.store_id and s.enabled = true
             where m.scene_key = #{sceneKey}
               and m.enabled = true
               and p.status = 'ON_SHELF'
               and s.id = (
                   select min(s2.id)
                     from product_sku s2
                    where s2.tenant_id = p.tenant_id
                      and s2.store_id = p.store_id
                      and s2.product_id = p.id
                      and s2.enabled = true
               )
             order by p.sort_order, p.id
            """)
    List<Map<String, Object>> selectMiniProductsByScene(@Param("sceneKey") String sceneKey);
}
