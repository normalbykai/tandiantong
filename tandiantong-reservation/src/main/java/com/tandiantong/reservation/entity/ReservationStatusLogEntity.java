package com.tandiantong.reservation.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 预约状态日志实体，记录预约状态变化。
 */
@Getter
@Setter
@TableName("reservation_status_log")
public class ReservationStatusLogEntity {

    /** 状态日志主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 预约单号。 */
    private String reservationNo;

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
