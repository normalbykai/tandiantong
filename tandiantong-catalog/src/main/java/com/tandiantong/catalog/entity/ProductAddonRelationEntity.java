package com.tandiantong.catalog.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 商品加料分组关联实体。
 */
@Getter
@Setter
@TableName("product_addon_relation")
public class ProductAddonRelationEntity {

    /** 关联主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 商品主键。 */
    private Long productId;

    /** 加料分组主键。 */
    private Long addonGroupId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
