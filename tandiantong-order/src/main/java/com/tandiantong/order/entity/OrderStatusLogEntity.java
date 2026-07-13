package com.tandiantong.order.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 订单状态日志实体，记录关键状态流转。
 */
@Getter
@Setter
@TableName("order_status_log")
public class OrderStatusLogEntity {

    /** 状态日志主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 平台业务订单号。 */
    private String orderNo;

    /** 原状态。 */
    private String fromStatus;

    /** 新状态。 */
    private String toStatus;

    /** 状态变化原因。 */
    private String reason;

    /** 操作人用户主键，系统操作时为空。 */
    private Long operatorUserId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
