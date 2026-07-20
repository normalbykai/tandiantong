package com.tandiantong.security.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 平台系统配置实体，保存平台管理端可维护的品牌信息和账号安全策略。 */
@Getter
@Setter
@TableName("platform_system_config")
public class PlatformSystemConfigEntity {

    /** 固定配置主键。 */
    @TableId
    private Long id;

    /** 平台 Logo 图片地址。 */
    private String logoUrl;

    /** 平台描述信息。 */
    private String description;

    /** 平台账号重置密码策略，取值为 RANDOM 或 FIXED。 */
    private String resetPasswordMode;

    /** 固定重置密码的 BCrypt 哈希，禁止在接口和日志中输出。 */
    private String fixedResetPasswordHash;

    /** 最近修改时间。 */
    private LocalDateTime updatedAt;

    /** 最近修改人主键。 */
    private Long updatedBy;
}
