package com.tandiantong.catalog.product;

/** 库存变化业务类型。 */
public enum InventoryChangeType {
    /** 商品创建或 SKU 初始化时写入初始库存。 */
    INITIAL_STOCK,
    /** 商户手工增加库存。 */
    MANUAL_IN,
    /** 商户手工减少库存。 */
    MANUAL_OUT,
    /** 商户盘点后调整库存。 */
    STOCKTAKE,
    /** 创建待支付订单时锁定库存。 */
    ORDER_LOCK,
    /** 订单取消或支付超时后释放锁定库存。 */
    ORDER_RELEASE,
    /** 支付成功后确认扣减库存。 */
    PAYMENT_DEDUCT,
    /** 退款成功后回补可售库存。 */
    REFUND_RESTORE
}
