package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 租户主表实体，记录商户租户的基础身份和启停状态。
 */
@Getter
@Setter
@TableName("tenant")
public class TenantEntity {

    /** 租户主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户编码，用于平台侧识别商户。 */
    private String tenantCode;

    /** 租户名称。 */
    private String name;

    /** 租户状态，控制是否允许继续产生业务写操作。 */
    private String status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
