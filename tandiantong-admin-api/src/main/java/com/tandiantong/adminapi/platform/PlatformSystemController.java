package com.tandiantong.adminapi.platform;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.context.SecurityContextHolder;
import com.tandiantong.security.entity.PlatformDictionaryItemEntity;
import com.tandiantong.security.entity.PlatformSystemConfigEntity;
import com.tandiantong.security.platform.PlatformSystemManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 平台系统管理接口，严格限定在平台权限域。 */
@RestController
@ConditionalOnProperty(
        prefix = "tandiantong.security",
        name = "database-enabled",
        havingValue = "true",
        matchIfMissing = true)
@RequestMapping("/api/platform/v1/system")
@Tag(name = "平台系统管理", description = "维护平台品牌信息和平台通用字典，不读取商户权限域数据")
public class PlatformSystemController {
    private final PlatformSystemManagementService service;

    public PlatformSystemController(PlatformSystemManagementService service) {
        this.service = service;
    }

    @GetMapping("/config")
    @SaCheckPermission("platform:system:security:read")
    @Operation(summary = "查询平台系统配置", description = "查询平台 Logo 图片地址和平台描述信息")
    public ConfigResponse getConfig() {
        return ConfigResponse.from(service.getConfig());
    }

    @PutMapping("/config")
    @SaCheckPermission("platform:system:security:update")
    @Operation(summary = "更新平台系统配置", description = "更新平台 Logo 图片地址和平台描述信息，不支持上传文件")
    public ConfigResponse updateConfig(@Valid @RequestBody ConfigRequest request) {
        return ConfigResponse.from(
                service.updateConfig(
                        current(),
                        request.logoUrl,
                        request.description,
                        request.resetPasswordMode,
                        request.fixedResetPassword));
    }

    @GetMapping("/dictionaries")
    @SaCheckPermission("platform:dictionary:read")
    @Operation(summary = "查询平台字典", description = "按字典类型查询平台通用字典项")
    public List<DictionaryResponse> listDictionaries(
            @RequestParam(value = "dictionaryType", required = false) String dictionaryType) {
        return service.listDictionaryItems(dictionaryType).stream()
                .map(DictionaryResponse::from)
                .toList();
    }

    @PostMapping("/dictionaries")
    @SaCheckPermission("platform:dictionary:create")
    @Operation(summary = "新增平台字典项", description = "新增字典类型下的字典项")
    public DictionaryResponse createDictionary(@Valid @RequestBody DictionaryRequest request) {
        return DictionaryResponse.from(
                service.createDictionaryItem(
                        current(),
                        request.dictionaryType,
                        request.itemCode,
                        request.itemValue,
                        request.itemLabel,
                        request.sortOrder));
    }

    @PutMapping("/dictionaries/{id}")
    @SaCheckPermission("platform:dictionary:update")
    @Operation(summary = "编辑平台字典项", description = "修改字典项名称和排序，不修改字典类型及编码")
    public void updateDictionary(
            @PathVariable("id") Long id, @Valid @RequestBody DictionaryUpdateRequest request) {
        service.updateDictionaryItem(current(), id, request.itemLabel, request.sortOrder);
    }

    @PostMapping("/dictionaries/{id}/status")
    @SaCheckPermission("platform:dictionary:status:update")
    @Operation(summary = "启停平台字典项", description = "更新字典项启用状态")
    public void updateDictionaryStatus(
            @PathVariable("id") Long id, @Valid @RequestBody StatusRequest request) {
        service.updateDictionaryStatus(current(), id, request.enabled);
    }

    private CurrentUser current() {
        return SecurityContextHolder.currentUser();
    }

    @Getter
    @Setter
    @Schema(description = "平台系统配置请求")
    public static class ConfigRequest {
        @NotBlank
        @Size(max = 1024)
        @Schema(description = "Logo 图片地址", example = "https://cdn.example.com/logo.png")
        private String logoUrl;

        @NotBlank
        @Size(max = 255)
        @Schema(description = "平台描述信息", example = "面向线下商户的经营管理平台")
        private String description;

        @NotBlank
        @Schema(description = "重置密码策略，RANDOM为随机生成，FIXED为固定密码", example = "RANDOM")
        private String resetPasswordMode;

        @Size(min = 8, max = 64)
        @Schema(description = "固定临时密码；随机策略无需填写", example = "Temp@123456")
        private String fixedResetPassword;
    }

    @Getter
    @Setter
    @Schema(description = "平台系统配置响应")
    public static class ConfigResponse {
        private String logoUrl;
        private String description;
        private String resetPasswordMode;
        private boolean fixedResetPasswordConfigured;

        static ConfigResponse from(PlatformSystemConfigEntity source) {
            ConfigResponse response = new ConfigResponse();
            response.logoUrl = source.getLogoUrl();
            response.description = source.getDescription();
            response.resetPasswordMode =
                    source.getResetPasswordMode() == null
                            ? "RANDOM"
                            : source.getResetPasswordMode();
            response.fixedResetPasswordConfigured = source.getFixedResetPasswordHash() != null;
            return response;
        }
    }

    @Getter
    @Setter
    @Schema(description = "新增平台字典项请求")
    public static class DictionaryRequest {
        @NotBlank
        @Size(max = 64)
        @Schema(description = "字典类型编码", example = "ORDER_STATUS")
        private String dictionaryType;

        @NotBlank
        @Size(max = 64)
        @Schema(description = "字典项编码", example = "PENDING")
        private String itemCode;

        @NotBlank
        @Size(max = 255)
        @Schema(description = "业务实际存储值", example = "pending")
        private String itemValue;

        @NotBlank
        @Size(max = 128)
        @Schema(description = "字典项名称", example = "待处理")
        private String itemLabel;

        @NotNull
        @Schema(description = "排序值", example = "10")
        private Integer sortOrder;
    }

    @Getter
    @Setter
    @Schema(description = "编辑平台字典项请求")
    public static class DictionaryUpdateRequest {
        @NotBlank
        @Size(max = 128)
        @Schema(description = "字典项名称", example = "待处理")
        private String itemLabel;

        @NotNull
        @Schema(description = "排序值", example = "10")
        private Integer sortOrder;
    }

    @Getter
    @Setter
    @Schema(description = "平台字典项响应")
    public static class DictionaryResponse {
        private Long id;
        private String dictionaryType;
        private String itemCode;
        private String itemValue;
        private String itemLabel;
        private Integer sortOrder;
        private String status;

        static DictionaryResponse from(PlatformDictionaryItemEntity source) {
            DictionaryResponse response = new DictionaryResponse();
            response.id = source.getId();
            response.dictionaryType = source.getDictionaryType();
            response.itemCode = source.getItemCode();
            response.itemValue = source.getItemValue();
            response.itemLabel = source.getItemLabel();
            response.sortOrder = source.getSortOrder();
            response.status = source.getStatus();
            return response;
        }
    }

    @Getter
    @Setter
    @Schema(description = "启停状态请求")
    public static class StatusRequest {
        private boolean enabled;
    }
}
