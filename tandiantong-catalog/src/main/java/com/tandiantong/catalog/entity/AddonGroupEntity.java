package com.tandiantong.catalog.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 加料分组实体，定义商品可选加料的选择规则。
 */
@Getter
@Setter
@TableName("addon_group")
public class AddonGroupEntity {

    /** 加料分组主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 加料分组名称。 */
    private String name;

    /** 是否必选。 */
    private Boolean required;

    /** 最少选择数量。 */
    private Integer minSelect;

    /** 最多选择数量。 */
    private Integer maxSelect;

    /** 是否启用。 */
    private Boolean enabled;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
