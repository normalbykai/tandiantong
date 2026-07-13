package com.tandiantong.catalog.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 商品 SKU 实体，记录规格价格和库存。
 */
@Getter
@Setter
@TableName("product_sku")
public class ProductSkuEntity {

    /** SKU 主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 所属商品主键。 */
    private Long productId;

    /** 规格展示文本。 */
    private String specificationText;

    /** SKU 编码。 */
    private String skuCode;

    /** SKU 售价，单位为分。 */
    private Integer priceCent;

    /** 可售库存数量。 */
    private Integer availableStock;

    /** 已锁定库存数量。 */
    private Integer lockedStock;

    /** 库存预警数量。 */
    private Integer warningStock;

    /** 是否启用。 */
    private Boolean enabled;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
