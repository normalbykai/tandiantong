package com.tandiantong.reservation.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务时段实体，记录某个服务在指定日期和时间段的容量。
 */
@Getter
@Setter
@TableName("service_slot")
public class ServiceSlotEntity {

    /** 服务时段主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 服务项目主键。 */
    private Long serviceId;

    /** 服务日期。 */
    private LocalDate serviceDate;

    /** 开始时间文本。 */
    private String startTime;

    /** 结束时间文本。 */
    private String endTime;

    /** 时段总容量。 */
    private Integer capacity;

    /** 已占用容量。 */
    private Integer usedCapacity;

    /** 是否暂停预约。 */
    private Boolean paused;

    /** 乐观版本号，用于容量并发控制。 */
    private Long version;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
