package com.tandiantong.security.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 权限实体，记录菜单、操作和 API 权限标识。
 */
@Getter
@Setter
@TableName("permission")
public class PermissionEntity {

    /** 权限主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限域。 */
    private String domain;

    /** 权限类型。 */
    private String permissionType;

    /** 权限编码。 */
    private String permissionCode;

    /** 权限名称。 */
    private String name;

    /** 创建时间。 */
    private LocalDateTime createdAt;
}
