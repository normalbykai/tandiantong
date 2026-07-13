package com.tandiantong.catalog.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 商品实体，记录商品基础信息和上下架状态。
 */
@Getter
@Setter
@TableName("product")
public class ProductEntity {

    /** 商品主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 所属分类主键。 */
    private Long categoryId;

    /** 商品名称。 */
    private String name;

    /** 商品图片地址。 */
    private String imageUrl;

    /** 商品描述。 */
    private String description;

    /** 基础价格，单位为分。 */
    private Integer basePriceCent;

    /** 商品状态。 */
    private String status;

    /** 排序值，数值越小越靠前。 */
    private Integer sortOrder;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
