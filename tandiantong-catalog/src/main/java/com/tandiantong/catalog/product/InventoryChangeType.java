package com.tandiantong.catalog.product;

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
