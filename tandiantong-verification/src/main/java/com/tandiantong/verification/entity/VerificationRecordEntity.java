package com.tandiantong.verification.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 核销记录实体，记录最终核销事实。
 */
@Getter
@Setter
@TableName("verification_record")
public class VerificationRecordEntity {

    /** 核销记录主键。 */
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

    /** 核销操作人用户主键。 */
    private Long operatorUserId;

    /** 核销原因。 */
    private String reason;

    /** 核销时间。 */
    private LocalDateTime verifiedAt;
}
