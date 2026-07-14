package com.tandiantong.adminapi.verification;

import com.tandiantong.adminapi.verification.dto.VerifyRequest;
import com.tandiantong.security.context.SecurityContextHolder;
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
@Tag(name = "商户核销")
public class AdminVerificationController {
    private final VerificationPersistenceService verificationPersistenceService;
    public AdminVerificationController(VerificationPersistenceService verificationPersistenceService) { this.verificationPersistenceService=verificationPersistenceService; }
    @Operation(summary = "核销业务凭证")
    @PostMapping
    public VerificationPersistenceService.VerificationResult verify(@Valid @RequestBody VerifyRequest request) {
        var user=SecurityContextHolder.currentUser();
        return verificationPersistenceService.verify(user.tenantId(),user.storeId(),user.userId(),request.token(),request.reason());
    }
}
