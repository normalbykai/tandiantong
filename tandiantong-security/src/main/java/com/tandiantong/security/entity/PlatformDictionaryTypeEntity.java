package com.tandiantong.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** 平台字典类型实体，存储字典类型的中文名称和描述信息。 */
@Getter
@Setter
@TableName("platform_dictionary_type")
public class PlatformDictionaryTypeEntity {

    /** 字典类型主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典类型编码。 */
    private String dictionaryType;

    /** 字典类型中文名称。 */
    private String typeLabel;

    /** 字典类型描述说明。 */
    private String description;

    /** 排序值，数字越小越靠前。 */
    private Integer sortOrder;

    /** 启用状态。 */
    private String status;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
