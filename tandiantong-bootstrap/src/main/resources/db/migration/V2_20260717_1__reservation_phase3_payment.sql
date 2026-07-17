ALTER TABLE service_reservation
    ADD COLUMN pay_amount_cent INT NOT NULL DEFAULT 0 COMMENT '预约支付金额，单位分' AFTER contact_mobile,
    ADD COLUMN prepay_id VARCHAR(128) NULL COMMENT '微信预支付标识' AFTER pay_amount_cent,
    ADD COLUMN expire_at DATETIME(3) NULL COMMENT '待支付预约过期时间' AFTER transaction_id,
    ADD UNIQUE KEY uk_service_reservation_global_no (reservation_no),
    ADD KEY idx_service_reservation_expire (tenant_id, store_id, status, expire_at),
    ADD CONSTRAINT chk_service_reservation_pay_amount CHECK (pay_amount_cent >= 0);
