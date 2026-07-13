package com.tandiantong.verification.app;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.verification.domain.BusinessType;
import com.tandiantong.verification.domain.VerificationCredential;
import com.tandiantong.verification.domain.VerificationRecord;
import com.tandiantong.verification.domain.VerificationStatus;
import com.tandiantong.verification.tenant.TenantStoreScope;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** 核销凭证签发和一次性核销领域服务。 */
public class VerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AtomicLong idSequence = new AtomicLong(1000);
    private final Map<String, Integer> pickupSequenceByDay = new LinkedHashMap<>();
    private final Map<String, VerificationCredential> credentialsByBusinessNo = new LinkedHashMap<>();
    private final Map<String, VerificationCredential> credentialsByToken = new LinkedHashMap<>();
    private final Map<String, VerificationRecord> recordsByBusinessNo = new LinkedHashMap<>();

    public VerificationCredential issueCredential(TenantStoreScope scope, BusinessType businessType,
                                                  String businessNo, String summary, LocalDate businessDate) {
        if (credentialsByBusinessNo.containsKey(businessNo)) {
            return credentialsByBusinessNo.get(businessNo);
        }
        String pickupNo = businessType == BusinessType.PRODUCT_ORDER
                ? nextPickupNo(scope, businessDate)
                : null;
        String token = secureToken();
        VerificationCredential credential = new VerificationCredential(idSequence.incrementAndGet(), scope.tenantId(),
                scope.storeId(), businessType, businessNo, summary, businessDate, pickupNo, token,
                VerificationStatus.PENDING);
        credentialsByBusinessNo.put(businessNo, credential);
        credentialsByToken.put(token, credential);
        return credential;
    }

    public VerificationRecord verifyByToken(TenantStoreScope scope, String token, String reason) {
        VerificationCredential credential = credentialsByToken.get(token);
        ensureCredentialBelongsToScope(scope, credential);
        if (recordsByBusinessNo.containsKey(credential.businessNo())) {
            return recordsByBusinessNo.get(credential.businessNo());
        }
        if (credential.status() != VerificationStatus.PENDING) {
            throw businessError("当前凭证状态不能核销");
        }
        VerificationCredential verified = credential.verified();
        credentialsByBusinessNo.put(credential.businessNo(), verified);
        credentialsByToken.put(token, verified);
        VerificationRecord record = new VerificationRecord(idSequence.incrementAndGet(), scope.tenantId(),
                scope.storeId(), credential.businessType(), credential.businessNo(), credential.summary(),
                VerificationStatus.VERIFIED, scope.operatorUserId(), reason, Instant.now());
        recordsByBusinessNo.put(credential.businessNo(), record);
        return record;
    }

    public VerificationCredential findCredential(TenantStoreScope scope, String businessNo) {
        VerificationCredential credential = credentialsByBusinessNo.get(businessNo);
        ensureCredentialBelongsToScope(scope, credential);
        return credential;
    }

    public List<VerificationRecord> records(TenantStoreScope scope, String businessNo) {
        VerificationCredential credential = findCredential(scope, businessNo);
        List<VerificationRecord> records = new ArrayList<>();
        if (recordsByBusinessNo.containsKey(credential.businessNo())) {
            records.add(recordsByBusinessNo.get(credential.businessNo()));
        }
        return List.copyOf(records);
    }

    private String nextPickupNo(TenantStoreScope scope, LocalDate businessDate) {
        String key = scope.tenantId() + ":" + scope.storeId() + ":" + businessDate;
        int next = pickupSequenceByDay.getOrDefault(key, 0) + 1;
        pickupSequenceByDay.put(key, next);
        return "A" + String.format("%03d", next);
    }

    private String secureToken() {
        return "vk-" + Long.toUnsignedString(RANDOM.nextLong(), 36)
                + Long.toUnsignedString(RANDOM.nextLong(), 36)
                + Long.toUnsignedString(RANDOM.nextLong(), 36);
    }

    private void ensureCredentialBelongsToScope(TenantStoreScope scope, VerificationCredential credential) {
        if (credential == null || !credential.tenantId().equals(scope.tenantId())
                || !credential.storeId().equals(scope.storeId())) {
            throw businessError("核销凭证不存在或不属于当前门店");
        }
    }

    private BusinessException businessError(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
