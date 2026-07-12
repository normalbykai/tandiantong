package com.tandiantong.catalog.product;

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
