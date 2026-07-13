package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 租户支付配置实体，记录门店微信支付配置状态。
 */
@Getter
@Setter
@TableName("tenant_payment_config")
public class TenantPaymentConfigEntity {

    /** 支付配置主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 配置状态。 */
    private String status;

    /** 配置完成时间。 */
    private LocalDateTime configuredAt;

    /** 验证通过时间。 */
    private LocalDateTime verifiedAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
