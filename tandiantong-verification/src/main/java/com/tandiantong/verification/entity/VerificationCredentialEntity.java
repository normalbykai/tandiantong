package com.tandiantong.verification.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 核销凭证实体，保存不可猜测凭证的哈希和业务摘要。
 */
@Getter
@Setter
@TableName("verification_credential")
public class VerificationCredentialEntity {

    /** 核销凭证主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 业务类型。 */
    private String businessType;

    /** 业务单号。 */
    private String businessNo;

    /** 业务摘要。 */
    private String summary;

    /** 营业日期。 */
    private LocalDate businessDate;

    /** 取餐号，仅用于识别展示。 */
    private String pickupNo;

    /** 核销令牌哈希，禁止保存明文令牌。 */
    private String tokenHash;

    /** 凭证状态。 */
    private String status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
