package com.tandiantong.order.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 支付记录实体，保存微信支付成功流水。
 */
@Getter
@Setter
@TableName("payment_record")
public class PaymentRecordEntity {

    /** 支付记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 平台业务订单号。 */
    private String orderNo;

    /** 微信支付流水号。 */
    private String transactionId;

    /** 支付金额，单位为分。 */
    private Integer amountCent;

    /** 支付状态。 */
    private String status;

    /** 支付成功时间。 */
    private LocalDateTime paidAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
