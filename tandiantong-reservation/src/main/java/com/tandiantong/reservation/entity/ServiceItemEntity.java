package com.tandiantong.reservation.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 服务项目实体，记录可预约服务的价格和时长。
 */
@Getter
@Setter
@TableName("service_item")
public class ServiceItemEntity {

    /** 服务项目主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 服务名称。 */
    private String name;

    /** 支付模式。 */
    private String paymentMode;

    /** 服务价格，单位为分。 */
    private Integer priceCent;

    /** 服务时长，单位为分钟。 */
    private Integer durationMinutes;

    /** 服务状态。 */
    private String status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
