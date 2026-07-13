package com.tandiantong.reservation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 预约业务幂等记录实体，映射统一业务幂等表中的预约记录。
 */
@Getter
@Setter
@TableName("business_idempotency_record")
public class ReservationIdempotencyRecordEntity {

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
