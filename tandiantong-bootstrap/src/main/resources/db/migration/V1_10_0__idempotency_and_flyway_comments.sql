ALTER TABLE business_idempotency_record COMMENT = '业务请求幂等记录表';

ALTER TABLE business_idempotency_record
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '幂等记录ID',
    MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '租户ID',
    MODIFY COLUMN idempotency_key VARCHAR(128) NOT NULL COMMENT '幂等键',
    MODIFY COLUMN business_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    MODIFY COLUMN business_no VARCHAR(64) NOT NULL COMMENT '业务单号',
    MODIFY COLUMN result_status VARCHAR(32) NOT NULL COMMENT '业务处理结果状态',
    MODIFY COLUMN created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间';

ALTER TABLE flyway_schema_history COMMENT = 'Flyway数据库迁移历史表';

ALTER TABLE flyway_schema_history
    MODIFY COLUMN installed_rank INT NOT NULL COMMENT '迁移安装顺序',
    MODIFY COLUMN version VARCHAR(50) NULL COMMENT '迁移版本号',
    MODIFY COLUMN description VARCHAR(200) NOT NULL COMMENT '迁移描述',
    MODIFY COLUMN type VARCHAR(20) NOT NULL COMMENT '迁移类型',
    MODIFY COLUMN script VARCHAR(1000) NOT NULL COMMENT '迁移脚本名称',
    MODIFY COLUMN checksum INT NULL COMMENT '迁移脚本校验值',
    MODIFY COLUMN installed_by VARCHAR(100) NOT NULL COMMENT '执行迁移的数据库用户',
    MODIFY COLUMN installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '迁移安装时间',
    MODIFY COLUMN execution_time INT NOT NULL COMMENT '迁移执行耗时，单位毫秒',
    MODIFY COLUMN success TINYINT(1) NOT NULL COMMENT '迁移是否成功';
