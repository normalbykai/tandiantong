package com.tandiantong.order.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 业务幂等记录实体，保证重复请求返回稳定结果。
 */
@Getter
@Setter
@TableName("business_idempotency_record")
public class BusinessIdempotencyRecordEntity {

    /** 幂等记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 幂等键。 */
    private String idempotencyKey;

    /** 业务类型。 */
    private String businessType;

    /** 业务单号。 */
    private String businessNo;

    /** 业务结果状态。 */
    private String resultStatus;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
