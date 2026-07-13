package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 门店实体，第一阶段每个租户默认只有一个门店。
 */
@Getter
@Setter
@TableName("store")
public class StoreEntity {

    /** 门店主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 门店名称。 */
    private String name;

    /** 门店状态。 */
    private String status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
