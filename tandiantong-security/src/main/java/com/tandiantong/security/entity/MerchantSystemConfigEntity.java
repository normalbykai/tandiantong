package com.tandiantong.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 商户独立展示配置实体，不读取平台系统配置。 */
@Getter
@Setter
@TableName("merchant_system_config")
public class MerchantSystemConfigEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long storeId;
    private String shortName;
    private String notice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
