ALTER TABLE refund_record
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0 COMMENT '退款重试次数',
    ADD COLUMN next_retry_at DATETIME(3) NULL COMMENT '下次重试时间',
    ADD COLUMN last_error_message VARCHAR(255) NULL COMMENT '最后一次失败原因',
    ADD COLUMN review_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_RETRY' COMMENT '人工排查状态';

ALTER TABLE sales_order
    ADD COLUMN cancel_reason VARCHAR(255) NULL COMMENT '取消原因',
    ADD COLUMN canceled_at DATETIME(3) NULL COMMENT '取消时间',
    ADD COLUMN expire_at DATETIME(3) NULL COMMENT '超时取消时间';
