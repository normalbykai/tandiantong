package com.tandiantong.reservation.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 服务预约实体，记录顾客预约单和凭证信息。
 */
@Getter
@Setter
@TableName("service_reservation")
public class ServiceReservationEntity {

    /** 预约主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 服务项目主键。 */
    private Long serviceId;

    /** 服务时段主键。 */
    private Long slotId;

    /** 预约单号。 */
    private String reservationNo;

    /** 预约状态。 */
    private String status;

    /** 联系手机号。 */
    private String contactMobile;

    /** 预约凭证码。 */
    private String voucherCode;

    /** 微信支付流水号。 */
    private String transactionId;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
