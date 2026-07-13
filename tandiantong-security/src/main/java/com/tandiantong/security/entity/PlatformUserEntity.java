package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 平台用户实体，承载平台管理员登录身份。
 */
@Getter
@Setter
@TableName("platform_user")
public class PlatformUserEntity {

    /** 平台用户主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录手机号。 */
    private String mobile;

    /** 展示名称。 */
    private String displayName;

    /** 密码哈希，禁止在日志和接口响应中输出。 */
    private String passwordHash;

    /** 用户状态。 */
    private String status;

    /** 令牌版本，用于批量失效历史登录态。 */
    private Integer tokenVersion;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
