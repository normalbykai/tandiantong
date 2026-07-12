package com.tandiantong.catalog.product;

public record ProductSkuProfile(
        Long skuId,
        Long productId,
        Long tenantId,
        Long storeId,
        String specificationText,
        int priceCent,
        int availableStock,
        int lockedStock,
        int warningStock,
        String skuCode
) {

    ProductSkuProfile withStock(int newAvailableStock, int newLockedStock) {
        return new ProductSkuProfile(skuId, productId, tenantId, storeId, specificationText, priceCent,
                newAvailableStock, newLockedStock, warningStock, skuCode);
    }
}
