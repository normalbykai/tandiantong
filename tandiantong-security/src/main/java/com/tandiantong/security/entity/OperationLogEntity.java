package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 操作日志实体，记录权限和关键管理操作的审计信息。
 */
@Getter
@Setter
@TableName("operation_log")
public class OperationLogEntity {

    /** 操作日志主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键，平台操作可为空。 */
    private Long tenantId;

    /** 所属门店主键，平台操作可为空。 */
    private Long storeId;

    /** 权限域。 */
    private String domain;

    /** 操作人主键。 */
    private Long operatorId;

    /** 操作类型。 */
    private String operationType;

    /** 操作对象类型。 */
    private String targetType;

    /** 操作对象主键或业务编号。 */
    private String targetId;

    /** 操作详情。 */
    private String detail;

    /** 请求追踪号。 */
    private String traceId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
