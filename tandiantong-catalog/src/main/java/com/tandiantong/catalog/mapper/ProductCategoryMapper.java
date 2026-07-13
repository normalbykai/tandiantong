package com.tandiantong.catalog.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tandiantong.catalog.entity.ProductCategoryEntity;

/**
 * 商品分类 Mapper。
 */
@InterceptorIgnore(tenantLine = "true")
public interface ProductCategoryMapper extends BaseMapper<ProductCategoryEntity> {
}
