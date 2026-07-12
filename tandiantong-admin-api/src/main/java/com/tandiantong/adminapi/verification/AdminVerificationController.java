package com.tandiantong.adminapi.verification;

import com.tandiantong.security.context.SecurityContextHolder;
import com.tandiantong.verification.app.VerificationPersistenceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/verification")
public class AdminVerificationController {
    private final VerificationPersistenceService verificationPersistenceService;
    public AdminVerificationController(VerificationPersistenceService verificationPersistenceService) { this.verificationPersistenceService=verificationPersistenceService; }
    @PostMapping
    public VerificationPersistenceService.VerificationResult verify(@Valid @RequestBody VerifyRequest request) {
        var user=SecurityContextHolder.currentUser();
        return verificationPersistenceService.verify(user.tenantId(),user.storeId(),user.userId(),request.token(),request.reason());
    }
    public record VerifyRequest(@NotBlank(message="核销凭证不能为空") String token,String reason) {}
}
