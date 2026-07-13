package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 小程序入口码实体，用于将顾客扫码入口解析到可信租户与门店。
 */
@Getter
@Setter
@TableName("mini_program_scene")
public class MiniProgramSceneEntity {

    /** 入口码主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键。 */
    private Long tenantId;

    /** 所属门店主键。 */
    private Long storeId;

    /** 小程序 scene 参数值。 */
    private String sceneKey;

    /** 是否启用。 */
    private Boolean enabled;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
