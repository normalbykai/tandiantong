package com.tandiantong.adminapi.verification;

import com.tandiantong.adminapi.verification.dto.VerifyRequest;
import com.tandiantong.adminapi.verification.dto.VerificationResponse;
import com.tandiantong.security.context.SecurityContextHolder;
import com.tandiantong.security.audit.OperationAuditService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.tandiantong.verification.app.VerificationPersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商户后台安全核销接口。 */
@RestController
@RequestMapping("/api/admin/v1/verification")
@Tag(name = "商户核销", description = "商户使用安全凭证核销商品订单或预约")
public class AdminVerificationController {

    private final VerificationPersistenceService verificationPersistenceService;
    private final OperationAuditService operationAuditService;

    public AdminVerificationController(VerificationPersistenceService verificationPersistenceService,
                                       OperationAuditService operationAuditService) {
        this.verificationPersistenceService = verificationPersistenceService;
        this.operationAuditService = operationAuditService;
    }

    @Operation(summary = "核销业务凭证", description = "校验凭证所属租户、门店和当前状态，确保重复或并发核销最多成功一次")
    @PostMapping
    @SaCheckPermission("verification:credential:verify")
    public VerificationResponse verify(@Valid @RequestBody VerifyRequest request) {
        var user=SecurityContextHolder.currentUser();
        var result = verificationPersistenceService.verify(
                user.tenantId(),user.storeId(),user.userId(),request.token(),request.reason());
        operationAuditService.record(user, "业务核销", "核销凭证", result.businessNo(), "已完成业务凭证核销");
        return VerificationResponse.from(result);
    }
}
