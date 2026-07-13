package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 用户角色关系实体。
 */
@Getter
@Setter
@TableName("user_role")
public class UserRoleEntity {

    /** 用户角色关系主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属租户主键，平台用户关系为空。 */
    private Long tenantId;

    /** 权限域。 */
    private String domain;

    /** 用户主键。 */
    private Long userId;

    /** 角色主键。 */
    private Long roleId;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
