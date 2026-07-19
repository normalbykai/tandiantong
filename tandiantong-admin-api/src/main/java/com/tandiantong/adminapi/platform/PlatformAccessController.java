package com.tandiantong.adminapi.platform;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.tandiantong.security.context.SecurityContextHolder;
import com.tandiantong.security.entity.PermissionEntity;
import com.tandiantong.security.entity.PlatformUserEntity;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.platform.PlatformAccessManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 平台账号、角色和权限点管理接口。 */
@RestController
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
@RequestMapping("/api/platform/v1/access")
@Tag(name = "平台权限管理", description = "平台账号、平台角色及平台权限点的受控管理接口")
public class PlatformAccessController {
    private final PlatformAccessManagementService service;
    public PlatformAccessController(PlatformAccessManagementService service) { this.service = service; }

    @GetMapping("/accounts") @SaCheckPermission("platform:account:read") @Operation(summary = "查询平台账号", description = "查询平台权限域账号及其角色，不返回密码哈希")
    public List<AccountResponse> listAccounts() { return service.listAccounts().stream().map(user -> AccountResponse.from(user, service.listRoleIds(user.getId()))).toList(); }
    @PostMapping("/accounts") @ResponseStatus(HttpStatus.CREATED) @SaCheckPermission("platform:account:create") @Operation(summary = "新增平台账号", description = "新增平台账号并分配当前操作者权限范围内的角色")
    public AccountResponse createAccount(@Valid @RequestBody AccountRequest request) { PlatformUserEntity user = service.createAccount(current(), request.mobile, request.displayName, request.password, request.roleIds); return AccountResponse.from(user, service.listRoleIds(user.getId())); }
    @PutMapping("/accounts/{userId}") @SaCheckPermission("platform:account:update") @Operation(summary = "编辑平台账号", description = "修改展示名称和角色，不允许跨权限范围分配角色")
    public void updateAccount(@PathVariable("userId") Long userId, @Valid @RequestBody AccountUpdateRequest request) { service.updateAccount(current(), userId, request.displayName, request.roleIds); }
    @PostMapping("/accounts/{userId}/status") @SaCheckPermission("platform:account:status:update") @Operation(summary = "启停平台账号", description = "停用后使历史登录态失效，不能停用当前登录账号")
    public void updateAccountStatus(@PathVariable("userId") Long userId, @Valid @RequestBody StatusRequest request) { service.updateAccountStatus(current(), userId, request.enabled); }
    @PostMapping("/accounts/{userId}/reset-password") @SaCheckPermission("platform:account:password:reset") @Operation(summary = "重置平台账号密码", description = "重置密码并使该账号历史登录态失效")
    public ResetPasswordResponse resetPassword(@PathVariable("userId") Long userId) { var result = service.resetPassword(current(), userId); return ResetPasswordResponse.from(result); }

    @GetMapping("/roles") @SaCheckPermission("platform:role:read") @Operation(summary = "查询平台角色", description = "查询平台权限域角色和角色状态")
    public List<RoleResponse> listRoles() { return service.listRoles().stream().map(RoleResponse::from).toList(); }
    @PostMapping("/roles") @ResponseStatus(HttpStatus.CREATED) @SaCheckPermission("platform:role:create") @Operation(summary = "新增平台角色", description = "新增非系统预置的平台角色")
    public RoleResponse createRole(@Valid @RequestBody RoleRequest request) { return RoleResponse.from(service.createRole(current(), request.name, request.roleCode, request.description)); }
    @PutMapping("/roles/{roleId}") @SaCheckPermission("platform:role:update") @Operation(summary = "编辑平台角色", description = "修改平台角色的名称和说明，角色标识创建后不可修改")
    public void updateRole(@PathVariable("roleId") Long roleId, @Valid @RequestBody RoleUpdateRequest request) { service.updateRole(current(), roleId, request.name, request.description); }
    @PostMapping("/roles/{roleId}/status") @SaCheckPermission("platform:role:status:update") @Operation(summary = "启停平台角色", description = "启用或停用平台角色")
    public void updateRoleStatus(@PathVariable("roleId") Long roleId, @Valid @RequestBody StatusRequest request) { service.updateRoleStatus(current(), roleId, request.enabled); }
    @GetMapping("/roles/{roleId}/permission-ids") @SaCheckPermission("platform:role:read") @Operation(summary = "查询角色权限", description = "查询指定平台角色已配置的平台权限点主键")
    public List<Long> listRolePermissions(@PathVariable("roleId") Long roleId) { return service.listPermissionIds(roleId); }
    @PutMapping("/roles/{roleId}/permission-ids") @SaCheckPermission("platform:role:permission:assign") @Operation(summary = "配置角色权限", description = "以完整权限点集合替换角色权限，不允许越权授予；系统预置角色仅允许调整权限范围")
    public void replaceRolePermissions(@PathVariable("roleId") Long roleId, @Valid @RequestBody PermissionIdsRequest request) { service.replaceRolePermissions(current(), roleId, request.permissionIds); }
    @GetMapping("/permissions") @SaCheckPermission("platform:permission:read") @Operation(summary = "查询平台权限点", description = "只读查询系统维护的平台权限点，禁止通过接口创建或修改")
    public List<PermissionResponse> listPermissions() { return service.listPermissions().stream().map(PermissionResponse::from).toList(); }
    private com.tandiantong.security.context.CurrentUser current() { return SecurityContextHolder.currentUser(); }

    @Getter @Setter @Schema(description = "新增平台账号请求") public static class AccountRequest { @NotBlank @Pattern(regexp = "1\\d{10}") @Schema(description = "登录手机号", example = "13900000000") private String mobile; @NotBlank @Schema(description = "账号展示名称", example = "王运营") private String displayName; @NotBlank @Size(min = 8, max = 64) @Schema(description = "初始密码", example = "Platform@123") private String password; @NotEmpty @Schema(description = "平台角色主键列表", example = "[100001]") private List<Long> roleIds; }
    @Getter @Setter @Schema(description = "编辑平台账号请求") public static class AccountUpdateRequest { @NotBlank @Schema(description = "账号展示名称", example = "王运营") private String displayName; @NotEmpty @Schema(description = "平台角色主键列表", example = "[100001]") private List<Long> roleIds; }
    @Getter @Setter @Schema(description = "平台角色请求") public static class RoleRequest { @NotBlank @Schema(description = "角色名称", example = "平台运营") private String name; @NotBlank @Pattern(regexp = "platfrom_[a-z0-9_]+") @Schema(description = "角色标识，平台角色固定使用 platfrom_ 前缀，创建后不可修改", example = "platfrom_operations") private String roleCode; @Size(max = 255) @Schema(description = "角色说明", example = "负责商户开通与运营") private String description; }
    @Getter @Setter @Schema(description = "编辑平台角色请求") public static class RoleUpdateRequest { @NotBlank @Schema(description = "角色名称", example = "平台运营") private String name; @Size(max = 255) @Schema(description = "角色说明", example = "负责商户开通与运营") private String description; }
    @Getter @Setter @Schema(description = "启停状态请求") public static class StatusRequest { @Schema(description = "是否启用", example = "true") private boolean enabled; }
    @Getter @Setter @Schema(description = "重置密码请求") public static class PasswordRequest { @NotBlank @Size(min = 8, max = 64) @Schema(description = "新密码", example = "Platform@123") private String password; }
    @Getter @Setter @Schema(description = "重置密码结果") public static class ResetPasswordResponse { private String temporaryPassword; private String mode; static ResetPasswordResponse from(com.tandiantong.security.platform.PlatformSystemManagementService.TemporaryPassword source) { ResetPasswordResponse response = new ResetPasswordResponse(); response.temporaryPassword = source.plainPassword(); response.mode = source.mode(); return response; } }
    @Getter @Setter @Schema(description = "权限点集合请求") public static class PermissionIdsRequest { @Schema(description = "平台权限点主键列表，可传空数组清空权限", example = "[200001,200006]") private List<Long> permissionIds; }
    @Getter @Setter @Schema(description = "平台账号响应") public static class AccountResponse { private Long id; private String mobile; private String displayName; private String status; private List<Long> roleIds; private LocalDateTime createdAt; static AccountResponse from(PlatformUserEntity user, List<Long> roleIds) { AccountResponse response = new AccountResponse(); response.id=user.getId(); response.mobile=user.getMobile(); response.displayName=user.getDisplayName(); response.status=user.getStatus(); response.roleIds=roleIds; response.createdAt=user.getCreatedAt(); return response; } }
    @Getter @Setter @Schema(description = "平台角色响应") public static class RoleResponse { private Long id; private String roleCode; private String name; private String description; private String status; private boolean systemRole; static RoleResponse from(RoleEntity role) { RoleResponse response = new RoleResponse(); response.id=role.getId(); response.roleCode=role.getRoleCode(); response.name=role.getName(); response.description=role.getDescription(); response.status=role.getStatus(); response.systemRole=Boolean.TRUE.equals(role.getSystemRole()); return response; } }
    @Getter @Setter @Schema(description = "平台权限点响应") public static class PermissionResponse { private Long id; private String permissionType; private String permissionCode; private String name; static PermissionResponse from(PermissionEntity permission) { PermissionResponse response = new PermissionResponse(); response.id=permission.getId(); response.permissionType=permission.getPermissionType(); response.permissionCode=permission.getPermissionCode(); response.name=permission.getName(); return response; } }
}
