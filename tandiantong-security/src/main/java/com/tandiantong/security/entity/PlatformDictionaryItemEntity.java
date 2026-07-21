package com.tandiantong.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 平台通用字典项实体。 */
@Getter
@Setter
@TableName("platform_dictionary_item")
public class PlatformDictionaryItemEntity {

    /** 字典项主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典类型编码。 */
    private String dictionaryType;

    /** 字典项编码。 */
    private String itemCode;

    /** 字典项标签颜色类型。 */
    private String tagType;

    /** 字典项名称。 */
    private String itemLabel;

    /** 排序值。 */
    private Integer sortOrder;

    /** 启用状态。 */
    private String status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
