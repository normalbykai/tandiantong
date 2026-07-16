package com.tandiantong.order.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 商品订单实体，记录订单主状态和支付金额。
 */
@Getter
@Setter
@TableName("sales_order")
public class SalesOrderEntity {

    /** 订单主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 顾客主键，匿名或未建档时为空。 */
    private Long customerId;

    /** 平台业务订单号。 */
    private String orderNo;

    /** 订单状态。 */
    private String status;

    /** 支付金额，单位为分。 */
    private Integer payAmountCent;

    /** 联系手机号。 */
    private String contactMobile;

    /** 取餐时间文本。 */
    private String pickupTimeText;

    /** 微信预支付编号。 */
    private String prepayId;

    /** 订单取消原因。 */
    private String cancelReason;

    /** 订单取消时间。 */
    private LocalDateTime canceledAt;

    /** 订单超时取消时间。 */
    private LocalDateTime expireAt;

    /** 支付成功时间。 */
    private LocalDateTime paidAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
