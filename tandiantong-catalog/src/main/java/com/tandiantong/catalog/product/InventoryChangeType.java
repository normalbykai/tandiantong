package com.tandiantong.catalog.product;

/** 库存变化业务类型。 */
public enum InventoryChangeType {
    INITIAL_STOCK,
    MANUAL_IN,
    MANUAL_OUT,
    STOCKTAKE,
    ORDER_LOCK,
    ORDER_RELEASE,
    PAYMENT_DEDUCT,
    REFUND_RESTORE
}
