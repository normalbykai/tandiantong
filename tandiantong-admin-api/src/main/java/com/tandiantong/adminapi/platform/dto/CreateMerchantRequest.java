package com.tandiantong.adminapi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 开通商户请求。 */
@Schema(description = "平台开通商户请求")
public record CreateMerchantRequest(
        @Schema(description = "商户名称", example = "星河便当")
        @NotBlank(message = "商户名称不能为空")
        String merchantName,

        @Schema(description = "默认门店地址", example = "上海市徐汇区示例路 88 号")
        @NotBlank(message = "门店地址不能为空")
        String storeAddress,

        @Schema(description = "商户管理员姓名", example = "李店长")
        @NotBlank(message = "管理员姓名不能为空")
        String adminName,

        @Schema(description = "商户管理员手机号", example = "13900000000")
        @Pattern(regexp = "1\\d{10}", message = "管理员手机号格式不正确")
        String adminMobile
) {
}
