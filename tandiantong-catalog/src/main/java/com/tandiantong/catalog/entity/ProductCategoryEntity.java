package com.tandiantong.catalog.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 商品分类实体，承载门店内商品分组。
 */
@Getter
@Setter
@TableName("product_category")
public class ProductCategoryEntity {

    /** 分类主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 分类名称。 */
    private String name;

    /** 排序值，数值越小越靠前。 */
    private Integer sortOrder;

    /** 是否启用。 */
    private Boolean enabled;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
