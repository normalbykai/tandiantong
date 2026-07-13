package com.tandiantong.order.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 商品订单明细实体，保存下单时的商品和 SKU 快照。
 */
@Getter
@Setter
@TableName("sales_order_item")
public class SalesOrderItemEntity {

    /** 订单明细主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 平台业务订单号。 */
    private String orderNo;

    /** SKU 主键。 */
    private Long skuId;

    /** 下单时商品名称快照。 */
    private String productName;

    /** 下单时 SKU 规格文本快照。 */
    private String skuText;

    /** 下单时加料快照。 */
    private String addonSnapshot;

    /** 单价，单位为分。 */
    private Integer unitPriceCent;

    /** 加料金额，单位为分。 */
    private Integer addonAmountCent;

    /** 购买数量。 */
    private Integer quantity;

    /** 小计金额，单位为分。 */
    private Integer subtotalCent;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
