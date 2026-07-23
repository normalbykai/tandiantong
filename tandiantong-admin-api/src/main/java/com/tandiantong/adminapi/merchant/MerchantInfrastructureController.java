package com.tandiantong.adminapi.merchant;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.context.SecurityContextHolder;
import com.tandiantong.security.entity.AdminUserEntity;
import com.tandiantong.security.entity.OperationLogEntity;
import com.tandiantong.security.entity.PermissionEntity;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.entity.StoreEntity;
import com.tandiantong.security.tenant.MerchantInfrastructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 商户基础设施接口，独立于平台管理接口和平台权限域。 */
@RestController
@RequestMapping("/api/admin/v1/merchant-infrastructure")
@Tag(name = "商户基础设施", description = "查询商户门店、员工、角色、权限和商户操作日志")
public class MerchantInfrastructureController {

    private final MerchantInfrastructureService service;

    public MerchantInfrastructureController(MerchantInfrastructureService service) {
        this.service = service;
    }

    @GetMapping("/store")
    @SaCheckPermission("tenant:store:view")
    @Operation(summary = "查询门店信息", description = "只查询当前登录商户和当前门店的基础资料")
    public StoreResponse getStore() {
        return StoreResponse.from(service.getStore(current()));
    }

    @PutMapping("/store")
    @SaCheckPermission("tenant:store:update")
    @Operation(summary = "更新门店信息", description = "只更新当前登录商户当前门店的名称和状态")
    public StoreResponse updateStore(@Valid @RequestBody StoreUpdateRequest request) {
        return StoreResponse.from(service.updateStore(current(), request.name, request.status));
    }

    @GetMapping("/staff")
    @SaCheckPermission("tenant:staff:view")
    @Operation(summary = "查询员工账号", description = "查询当前商户当前门店的员工账号")
    public List<StaffResponse> listStaff() {
        return service.listStaff(current()).stream().map(StaffResponse::from).toList();
    }

    @org.springframework.web.bind.annotation.PostMapping("/staff")
    @SaCheckPermission("tenant:staff:update")
    @Operation(summary = "新增员工账号", description = "在当前商户当前门店新增员工账号并分配一个商户角色")
    public StaffResponse createStaff(@Valid @RequestBody StaffCreateRequest request) {
        return StaffResponse.from(service.createStaff(current(), request.mobile, request.displayName, request.password, request.roleId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/staff/{staffId}")
    @SaCheckPermission("tenant:staff:update")
    @Operation(summary = "编辑员工账号", description = "编辑当前商户当前门店员工资料和角色")
    public StaffResponse updateStaff(@org.springframework.web.bind.annotation.PathVariable("staffId") Long staffId, @Valid @RequestBody StaffUpdateRequest request) {
        return StaffResponse.from(service.updateStaff(current(), staffId, request.mobile, request.displayName, request.roleId));
    }

    @org.springframework.web.bind.annotation.PostMapping("/staff/{staffId}/status")
    @SaCheckPermission("tenant:staff:update")
    @Operation(summary = "启停员工账号", description = "启用或停用当前商户当前门店员工账号")
    public void updateStaffStatus(@org.springframework.web.bind.annotation.PathVariable("staffId") Long staffId, @Valid @RequestBody StatusRequest request) {
        service.updateStaffStatus(current(), staffId, request.status);
    }

    @GetMapping("/roles")
    @SaCheckPermission("tenant:role:view")
    @Operation(summary = "查询商户角色", description = "查询当前商户权限域中的角色")
    public List<RoleResponse> listRoles() {
        return service.listRoles(current()).stream().map(RoleResponse::from).toList();
    }

    @org.springframework.web.bind.annotation.PostMapping("/roles")
    @SaCheckPermission("tenant:role:update")
    @Operation(summary = "新增商户角色", description = "新增当前商户权限域角色")
    public RoleResponse createRole(@Valid @RequestBody RoleCreateRequest request) {
        return RoleResponse.from(service.createRole(current(), request.roleCode, request.name, request.description));
    }

    @org.springframework.web.bind.annotation.PutMapping("/roles/{roleId}")
    @SaCheckPermission("tenant:role:update")
    @Operation(summary = "编辑商户角色", description = "编辑当前商户自定义角色名称和说明")
    public RoleResponse updateRole(@org.springframework.web.bind.annotation.PathVariable("roleId") Long roleId, @Valid @RequestBody RoleUpdateRequest request) {
        return RoleResponse.from(service.updateRole(current(), roleId, request.name, request.description));
    }

    @org.springframework.web.bind.annotation.PostMapping("/roles/{roleId}/status")
    @SaCheckPermission("tenant:role:update")
    @Operation(summary = "启停商户角色", description = "启用或停用当前商户自定义角色")
    public void updateRoleStatus(@org.springframework.web.bind.annotation.PathVariable("roleId") Long roleId, @Valid @RequestBody StatusRequest request) {
        service.updateRoleStatus(current(), roleId, request.status);
    }

    @GetMapping("/roles/{roleId}/permission-ids")
    @SaCheckPermission("tenant:role:view")
    @Operation(summary = "查询商户角色权限", description = "查询当前商户角色已经分配的商户权限点")
    public List<Long> listRolePermissionIds(@org.springframework.web.bind.annotation.PathVariable("roleId") Long roleId) {
        return service.listRolePermissionIds(current(), roleId);
    }

    @PutMapping("/roles/{roleId}/permission-ids")
    @SaCheckPermission("tenant:role:update")
    @Operation(summary = "配置商户角色权限", description = "替换当前商户角色的商户权限点，禁止配置平台权限")
    public void replaceRolePermissions(@org.springframework.web.bind.annotation.PathVariable("roleId") Long roleId, @RequestBody PermissionIdsRequest request) {
        service.replaceRolePermissions(current(), roleId, request.permissionIds == null ? List.of() : request.permissionIds);
    }

    @GetMapping("/permissions")
    @SaCheckPermission("tenant:permission:read")
    @Operation(summary = "查询商户权限", description = "仅返回TENANT权限域权限点")
    public List<PermissionResponse> listPermissions() {
        return service.listPermissions().stream().map(PermissionResponse::from).toList();
    }

    @GetMapping("/system-config")
    @SaCheckPermission("tenant:system:view")
    @Operation(summary = "查询商户展示设置", description = "查询当前商户当前门店自己的展示设置，不读取平台配置")
    public SystemConfigResponse getSystemConfig() {
        return SystemConfigResponse.from(service.getSystemConfig(current()));
    }

    @PutMapping("/system-config")
    @SaCheckPermission("tenant:system:update")
    @Operation(summary = "保存商户展示设置", description = "保存当前商户当前门店自己的简称和公告")
    public SystemConfigResponse updateSystemConfig(@Valid @RequestBody SystemConfigRequest request) {
        return SystemConfigResponse.from(service.updateSystemConfig(current(), request.shortName, request.notice));
    }

    @GetMapping("/logs")
    @SaCheckPermission("tenant:operation-log:read")
    @Operation(summary = "查询商户操作日志", description = "按当前商户和门店范围分页查询审计记录")
    public List<LogResponse> listLogs(@RequestParam(value = "keyword", required = false) String keyword,
                                      @RequestParam(value = "operationType", required = false) String operationType,
                                      @RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return service.listLogs(current(), keyword, operationType, page, pageSize).stream().map(LogResponse::from).toList();
    }

    private CurrentUser current() {
        return SecurityContextHolder.currentUser();
    }

    @Getter
    @Setter
    @Schema(description = "门店更新请求")
    public static class StoreUpdateRequest {
        @NotBlank
        @Size(max = 128)
        private String name;

        @NotBlank
        @Pattern(regexp = "ENABLED|DISABLED")
        private String status;
    }

    @Getter @Setter @Schema(description = "员工新增请求")
    public static class StaffCreateRequest { @NotBlank @Pattern(regexp = "1\\d{10}") private String mobile; @NotBlank @Size(max = 64) private String displayName; @NotBlank @Size(min = 8, max = 64) private String password; @jakarta.validation.constraints.NotNull private Long roleId; }
    @Getter @Setter @Schema(description = "员工编辑请求")
    public static class StaffUpdateRequest { @NotBlank @Pattern(regexp = "1\\d{10}") private String mobile; @NotBlank @Size(max = 64) private String displayName; @jakarta.validation.constraints.NotNull private Long roleId; }
    @Getter @Setter @Schema(description = "角色新增请求")
    public static class RoleCreateRequest { @NotBlank @Pattern(regexp = "merchant_[a-z0-9_]+") private String roleCode; @NotBlank @Size(max = 64) private String name; @Size(max = 255) private String description; }
    @Getter @Setter @Schema(description = "角色编辑请求")
    public static class RoleUpdateRequest { @NotBlank @Size(max = 64) private String name; @Size(max = 255) private String description; }
    @Getter @Setter @Schema(description = "状态更新请求")
    public static class StatusRequest { @NotBlank @Pattern(regexp = "ENABLED|DISABLED") private String status; }
    @Getter @Setter @Schema(description = "角色权限请求")
    public static class PermissionIdsRequest { private List<Long> permissionIds = new ArrayList<>(); }
    @Getter @Setter @Schema(description = "商户展示设置请求")
    public static class SystemConfigRequest { @NotBlank @Size(max = 64) private String shortName; @Size(max = 255) private String notice; }

    @Getter
    @Schema(description = "门店信息响应")
    public static class StoreResponse {
        private Long id;
        private Long tenantId;
        private String name;
        private String status;

        static StoreResponse from(StoreEntity source) {
            StoreResponse response = new StoreResponse();
            response.id = source.getId(); response.tenantId = source.getTenantId(); response.name = source.getName(); response.status = source.getStatus();
            return response;
        }
    }

    @Getter
    @Schema(description = "员工账号响应")
    public static class StaffResponse {
        private Long id; private String mobile; private String displayName; private String status; private LocalDateTime lastLoginAt;
        static StaffResponse from(AdminUserEntity source) { StaffResponse response = new StaffResponse(); response.id = source.getId(); response.mobile = source.getMobile(); response.displayName = source.getDisplayName(); response.status = source.getStatus(); return response; }
    }

    @Getter
    @Schema(description = "商户角色响应")
    public static class RoleResponse {
        private Long id; private String roleCode; private String name; private String description; private String status; private Boolean systemRole;
        static RoleResponse from(RoleEntity source) { RoleResponse response = new RoleResponse(); response.id = source.getId(); response.roleCode = source.getRoleCode(); response.name = source.getName(); response.description = source.getDescription(); response.status = source.getStatus(); response.systemRole = source.getSystemRole(); return response; }
    }

    @Getter @Schema(description = "商户展示设置响应")
    public static class SystemConfigResponse { private Long id; private Long tenantId; private Long storeId; private String shortName; private String notice; static SystemConfigResponse from(com.tandiantong.security.entity.MerchantSystemConfigEntity source) { SystemConfigResponse response = new SystemConfigResponse(); response.id = source.getId(); response.tenantId = source.getTenantId(); response.storeId = source.getStoreId(); response.shortName = source.getShortName(); response.notice = source.getNotice(); return response; } }

    @Getter
    @Schema(description = "商户权限响应")
    public static class PermissionResponse {
        private Long id; private String permissionType; private String permissionCode; private String name;
        static PermissionResponse from(PermissionEntity source) { PermissionResponse response = new PermissionResponse(); response.id = source.getId(); response.permissionType = source.getPermissionType(); response.permissionCode = source.getPermissionCode(); response.name = source.getName(); return response; }
    }

    @Getter
    @Schema(description = "商户操作日志响应")
    public static class LogResponse {
        private Long id; private String operationType; private String targetType; private String targetId; private String detail; private String traceId; private LocalDateTime createdAt;
        static LogResponse from(OperationLogEntity source) { LogResponse response = new LogResponse(); response.id = source.getId(); response.operationType = source.getOperationType(); response.targetType = source.getTargetType(); response.targetId = source.getTargetId(); response.detail = source.getDetail(); response.traceId = source.getTraceId(); response.createdAt = source.getCreatedAt(); return response; }
    }
}
