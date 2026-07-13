package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 商户管理员邀请实体，记录平台开通商户后的激活凭据。
 */
@Getter
@Setter
@TableName("merchant_invitation")
public class MerchantInvitationEntity {

    /** 邀请记录主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 被邀请管理员姓名。 */
    private String adminName;

    /** 被邀请管理员手机号。 */
    private String adminMobile;

    /** 邀请码哈希，禁止保存明文邀请码。 */
    private String invitationCodeHash;

    /** 邀请过期时间。 */
    private LocalDateTime expiresAt;

    /** 邀请使用时间，未使用时为空。 */
    private LocalDateTime usedAt;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
