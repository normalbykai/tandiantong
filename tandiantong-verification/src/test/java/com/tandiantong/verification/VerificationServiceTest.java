package com.tandiantong.verification;

import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.verification.app.VerificationService;
import com.tandiantong.verification.domain.BusinessType;
import com.tandiantong.verification.domain.VerificationStatus;
import com.tandiantong.verification.tenant.TenantStoreScope;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationServiceTest {

    private final TenantStoreScope scope = new TenantStoreScope(1001L, 2001L, 3001L);

    @Test
    void shouldGeneratePickupNoByBusinessDayAndIssueSecureToken() {
        VerificationService service = new VerificationService();

        var credential = service.issueCredential(scope, BusinessType.PRODUCT_ORDER, "SO1001001",
                "桂花拿铁 ×2", LocalDate.of(2026, 7, 12));

        assertThat(credential.pickupNo()).isEqualTo("A001");
        assertThat(credential.verificationToken()).startsWith("vk-");
        assertThat(credential.verificationToken()).hasSizeGreaterThan(24);
    }

    @Test
    void shouldRejectPickupNoAsVerificationToken() {
        VerificationService service = new VerificationService();
        var credential = service.issueCredential(scope, BusinessType.PRODUCT_ORDER, "SO1001002",
                "桂花拿铁 ×2", LocalDate.of(2026, 7, 12));

        assertThatThrownBy(() -> service.verifyByToken(scope, credential.pickupNo(), "扫码核销"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("核销凭证不存在或不属于当前门店");
    }

    @Test
    void shouldVerifyProductOrderOnlyOnce() {
        VerificationService service = new VerificationService();
        var credential = service.issueCredential(scope, BusinessType.PRODUCT_ORDER, "SO1001003",
                "手作三明治 ×1", LocalDate.of(2026, 7, 12));

        var first = service.verifyByToken(scope, credential.verificationToken(), "顾客到店取餐");
        var repeated = service.verifyByToken(scope, credential.verificationToken(), "重复扫码");

        assertThat(first.status()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(repeated.recordId()).isEqualTo(first.recordId());
        assertThat(service.findCredential(scope, credential.businessNo()).status()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(service.records(scope, credential.businessNo())).hasSize(1);
    }

    @Test
    void shouldRejectCrossTenantVerification() {
        VerificationService service = new VerificationService();
        var credential = service.issueCredential(scope, BusinessType.PRODUCT_ORDER, "SO1001004",
                "桂花拿铁 ×1", LocalDate.of(2026, 7, 12));
        TenantStoreScope otherTenant = new TenantStoreScope(1002L, 2002L, 3002L);

        assertThatThrownBy(() -> service.verifyByToken(otherTenant, credential.verificationToken(), "越权核销"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("核销凭证不存在或不属于当前门店");
    }

    @Test
    void shouldVerifyReservationCredential() {
        VerificationService service = new VerificationService();
        var credential = service.issueCredential(scope, BusinessType.RESERVATION, "YY202607130018",
                "咖啡体验课 14:00-15:00", LocalDate.of(2026, 7, 13));

        var record = service.verifyByToken(scope, credential.verificationToken(), "预约到店履约");

        assertThat(record.businessType()).isEqualTo(BusinessType.RESERVATION);
        assertThat(record.summary()).contains("咖啡体验课");
    }
}
