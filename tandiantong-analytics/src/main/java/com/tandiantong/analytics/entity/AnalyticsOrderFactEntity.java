package com.tandiantong.analytics.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单经营事实实体，用于按商品和日期统计销售数据。
 */
@Getter
@Setter
@TableName("analytics_order_fact")
public class AnalyticsOrderFactEntity {

    /** 订单事实主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 平台业务订单号。 */
    private String orderNo;

    /** 业务日期。 */
    private LocalDate businessDate;

    /** 订单状态。 */
    private String status;

    /** 销售金额，单位为分。 */
    private Integer grossAmountCent;

    /** 退款金额，单位为分。 */
    private Integer refundAmountCent;

    /** 销售数量。 */
    private Integer quantity;

    /** 商品名称快照。 */
    private String productName;

    /** SKU 名称快照。 */
    private String skuName;

    /** 加料名称快照。 */
    private String addonNames;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
