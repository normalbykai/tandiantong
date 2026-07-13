package com.tandiantong.catalog.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 库存流水实体，记录每一次库存变化后的数量快照。
 */
@Getter
@Setter
@TableName("inventory_record")
public class InventoryRecordEntity {

    /** 库存流水主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** SKU 主键。 */
    private Long skuId;

    /** 库存变化类型。 */
    private String changeType;

    /** 本次变化数量。 */
    private Integer quantity;

    /** 变化后的可售库存。 */
    private Integer availableAfter;

    /** 变化后的锁定库存。 */
    private Integer lockedAfter;

    /** 关联业务单号。 */
    private String businessNo;

    /** 变化原因。 */
    private String reason;

    /** 操作人用户主键。 */
    private Long operatorUserId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
