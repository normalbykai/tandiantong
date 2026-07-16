package com.tandiantong.order.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 退款记录实体，记录整单退款请求和结果。
 */
@Getter
@Setter
@TableName("refund_record")
public class RefundRecordEntity {

    /** 退款记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 平台业务订单号。 */
    private String orderNo;

    /** 平台退款单号。 */
    private String refundNo;

    /** 退款金额，单位为分。 */
    private Integer amountCent;

    /** 退款状态。 */
    private String status;

    /** 退款原因。 */
    private String reason;

    /** 退款重试次数。 */
    private Integer retryCount;

    /** 下次重试时间。 */
    private LocalDateTime nextRetryAt;

    /** 最后一次失败原因。 */
    private String lastErrorMessage;

    /** 人工排查状态。 */
    private String reviewStatus;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
