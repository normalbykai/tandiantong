package com.tandiantong.verification.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 取餐号序列表实体，按租户、门店和营业日生成取餐号。
 */
@Getter
@Setter
@TableName("pickup_no_sequence")
public class PickupNoSequenceEntity {

    /** 序列主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 营业日期。 */
    private LocalDate businessDate;

    /** 当前序列值。 */
    private Integer currentValue;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
