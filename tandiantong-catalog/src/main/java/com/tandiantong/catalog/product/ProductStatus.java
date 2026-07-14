package com.tandiantong.catalog.product;

/** 商品上下架状态。 */
public enum ProductStatus {
    /** 商品草稿，尚未进入售卖。 */
    DRAFT,
    /** 商品已上架，可被顾客购买。 */
    ON_SHELF,
    /** 商品已下架，不再对顾客售卖。 */
    OFF_SHELF
}
