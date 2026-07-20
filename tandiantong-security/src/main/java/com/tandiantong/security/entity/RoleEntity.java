package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 角色实体，区分平台和租户权限域。
 */
@Getter
@Setter
@TableName("role")
public class RoleEntity {

    /** 角色主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键，平台角色为空。 */
    private Long tenantId;

    /** 权限域。 */
    private String domain;

    /** 角色标识，用于程序稳定识别。 */
    private String roleCode;

    /** 角色名称。 */
    private String name;

    /** 角色说明。 */
    private String description;

    /** 角色状态。 */
    private String status;

    /** 是否系统预置角色。 */
    private Boolean systemRole;

    /** 是否为拥有受保护权限管理能力的平台角色。 */
    @TableField("is_authority_role")
    private Boolean authorityRole;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
