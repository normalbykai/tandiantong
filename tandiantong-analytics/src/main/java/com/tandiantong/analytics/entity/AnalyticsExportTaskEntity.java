package com.tandiantong.analytics.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 经营数据导出任务实体，记录导出审计信息。
 */
@Getter
@Setter
@TableName("analytics_export_task")
public class AnalyticsExportTaskEntity {

    /** 导出任务主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 导出类型。 */
    private String exportType;

    /** 导出开始日期。 */
    private LocalDate startDate;

    /** 导出结束日期。 */
    private LocalDate endDate;

    /** 导出文件名。 */
    private String fileName;

    /** 导出任务状态。 */
    private String status;

    /** 操作人用户主键。 */
    private Long operatorUserId;

    /** 脱敏后的操作人联系方式。 */
    private String operatorContactMasked;

    /** 审计消息。 */
    private String auditMessage;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 完成时间。 */
    private LocalDateTime finishedAt;
}
