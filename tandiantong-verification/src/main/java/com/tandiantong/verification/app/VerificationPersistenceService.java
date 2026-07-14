package com.tandiantong.verification.app;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.verification.entity.VerificationCredentialEntity;
import com.tandiantong.verification.entity.VerificationRecordEntity;
import com.tandiantong.verification.mapper.PickupNoSequenceMapper;
import com.tandiantong.verification.mapper.VerificationCredentialMapper;
import com.tandiantong.verification.mapper.VerificationRecordMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 核销应用服务，负责安全凭证签发、一次性核销、核销审计和业务状态通知。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class VerificationPersistenceService {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final String DEFAULT_VERIFY_REASON = "正常核销";

    private final PickupNoSequenceMapper pickupNoSequenceMapper;
    private final VerificationCredentialMapper verificationCredentialMapper;
    private final VerificationRecordMapper verificationRecordMapper;
    private final List<VerificationBusinessCompletionHandler> completionHandlers;

    /**
     * 为已支付商品订单签发不可猜测核销令牌和当日取餐号。
     */
    @Transactional
    public Credential issueOrderCredential(Long tenantId, Long storeId, String orderNo, String summary) {
        VerificationCredentialEntity existing = verificationCredentialMapper.selectOne(
                Wrappers.<VerificationCredentialEntity>lambdaQuery()
                        .eq(VerificationCredentialEntity::getTenantId, tenantId)
                        .eq(VerificationCredentialEntity::getStoreId, storeId)
                        .eq(VerificationCredentialEntity::getBusinessType, BusinessType.PRODUCT_ORDER.code())
                        .eq(VerificationCredentialEntity::getBusinessNo, orderNo));
        if (existing != null) {
            return new Credential(existing.getBusinessNo(), existing.getPickupNo(), null, existing.getStatus());
        }

        LocalDate businessDate = LocalDate.now(BUSINESS_ZONE_ID);
        pickupNoSequenceMapper.increment(tenantId, storeId, businessDate);
        Integer current = pickupNoSequenceMapper.selectCurrentValue(tenantId, storeId, businessDate);
        if (current == null) {
            throw new IllegalStateException("数据库未返回取餐号序列");
        }
        String token = "vk-" + randomPart() + randomPart();
        String pickupNo = "A" + String.format("%03d", current);

        VerificationCredentialEntity credential = new VerificationCredentialEntity();
        credential.setTenantId(tenantId);
        credential.setStoreId(storeId);
        credential.setBusinessType(BusinessType.PRODUCT_ORDER.code());
        credential.setBusinessNo(orderNo);
        credential.setSummary(summary);
        credential.setBusinessDate(businessDate);
        credential.setPickupNo(pickupNo);
        credential.setTokenHash(sha256(token));
        credential.setStatus(CredentialStatus.PENDING.code());
        verificationCredentialMapper.insert(credential);
        return new Credential(orderNo, pickupNo, token, CredentialStatus.PENDING.code());
    }

    /**
     * 使用凭证令牌执行一次性核销，重复提交已核销令牌返回同一结果。
     */
    @Transactional
    public VerificationResult verify(Long tenantId, Long storeId, Long operatorId,
                                     String token, String reason) {
        String tokenHash = sha256(token);
        VerificationCredentialEntity credential = verificationCredentialMapper.selectOne(
                Wrappers.<VerificationCredentialEntity>lambdaQuery()
                        .eq(VerificationCredentialEntity::getTenantId, tenantId)
                        .eq(VerificationCredentialEntity::getStoreId, storeId)
                        .eq(VerificationCredentialEntity::getTokenHash, tokenHash));
        if (credential == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "核销凭证不存在");
        }
        int updated = verificationCredentialMapper.updateStatusByToken(tenantId, storeId, tokenHash,
                CredentialStatus.PENDING.code(), CredentialStatus.VERIFIED.code());
        if (updated == 0 && !CredentialStatus.VERIFIED.matches(credential.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "当前凭证不能核销");
        }
        if (updated == 1) {
            insertVerificationRecord(credential, operatorId, reason);
            completionHandlers.stream()
                    .filter(handler -> handler.supports(credential.getBusinessType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("未找到核销业务完成处理器"))
                    .complete(tenantId, storeId, credential.getBusinessNo());
        }
        return new VerificationResult(credential.getBusinessNo(), credential.getPickupNo(),
                CredentialStatus.VERIFIED.code());
    }

    /**
     * 订单退款成功后取消尚未核销的凭证。
     */
    public void cancelOrderCredential(Long tenantId, Long storeId, String orderNo) {
        verificationCredentialMapper.updateBusinessStatus(tenantId, storeId,
                BusinessType.PRODUCT_ORDER.code(), orderNo,
                CredentialStatus.PENDING.code(), CredentialStatus.CANCELED.code());
    }

    private void insertVerificationRecord(VerificationCredentialEntity credential,
                                          Long operatorId, String reason) {
        VerificationRecordEntity record = new VerificationRecordEntity();
        record.setTenantId(credential.getTenantId());
        record.setStoreId(credential.getStoreId());
        record.setBusinessType(credential.getBusinessType());
        record.setBusinessNo(credential.getBusinessNo());
        record.setSummary(credential.getSummary());
        record.setOperatorUserId(operatorId);
        record.setReason(reason == null || reason.isBlank() ? DEFAULT_VERIFY_REASON : reason);
        record.setVerifiedAt(LocalDateTime.now(BUSINESS_ZONE_ID));
        verificationRecordMapper.insert(record);
    }

    private String randomPart() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String sha256(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境缺少 SHA-256 摘要算法", exception);
        }
    }

    /** 核销凭证签发结果，令牌明文只在首次签发时返回。 */
    public record Credential(String businessNo, String pickupNo, String token, String status) {
    }

    /** 核销处理结果。 */
    public record VerificationResult(String businessNo, String pickupNo, String status) {
    }

    /** 支持核销的业务类型。 */
    private enum BusinessType {
        /** 商品订单核销凭证。 */
        PRODUCT_ORDER;

        String code() {
            return name();
        }
    }

    /** 核销凭证状态。 */
    private enum CredentialStatus {
        /** 凭证待核销。 */
        PENDING,
        /** 凭证已核销。 */
        VERIFIED,
        /** 凭证已取消。 */
        CANCELED;

        String code() {
            return name();
        }

        boolean matches(String value) {
            return code().equals(value);
        }
    }
}
