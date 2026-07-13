package com.tandiantong.catalog.product;

/** 商品聚合查询资料。 */
public record ProductProfile(
        Long productId,
        Long tenantId,
        Long storeId,
        String productName,
        String categoryName,
        int basePriceCent,
        ProductStatus status
) {
}
