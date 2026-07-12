package com.tandiantong.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING_VERIFY,
    COMPLETED,
    CANCELED,
    REFUNDING,
    REFUNDED
}
