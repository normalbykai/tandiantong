package com.tandiantong.analytics.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约经营事实实体，用于统计预约履约和容量使用情况。
 */
@Getter
@Setter
@TableName("analytics_reservation_fact")
public class AnalyticsReservationFactEntity {

    /** 预约事实主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 预约单号。 */
    private String reservationNo;

    /** 业务日期。 */
    private LocalDate businessDate;

    /** 预约状态。 */
    private String status;

    /** 时段总容量。 */
    private Integer slotCapacity;

    /** 时段已使用容量。 */
    private Integer slotUsedCapacity;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
