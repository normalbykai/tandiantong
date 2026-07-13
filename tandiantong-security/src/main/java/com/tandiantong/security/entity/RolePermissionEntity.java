package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 角色权限关系实体。
 */
@Getter
@Setter
@TableName("role_permission")
public class RolePermissionEntity {

    /** 角色权限关系主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键，平台角色关系为空。 */
    private Long tenantId;

    /** 权限域。 */
    private String domain;

    /** 角色主键。 */
    private Long roleId;

    /** 权限主键。 */
    private Long permissionId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
