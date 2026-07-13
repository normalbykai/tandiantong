package com.tandiantong.catalog.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 加料选项实体，记录单个可选加料和加价金额。
 */
@Getter
@Setter
@TableName("addon_option")
public class AddonOptionEntity {

    /** 加料选项主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 所属加料分组主键。 */
    private Long addonGroupId;

    /** 加料选项名称。 */
    private String name;

    /** 加价金额，单位为分。 */
    private Integer priceCent;

    /** 是否启用。 */
    private Boolean enabled;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
