package com.tandiantong.security.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.security.auth.PasswordService;
import com.tandiantong.security.entity.AdminUserEntity;
import com.tandiantong.security.entity.MerchantInvitationEntity;
import com.tandiantong.security.entity.MiniProgramSceneEntity;
import com.tandiantong.security.entity.StoreEntity;
import com.tandiantong.security.entity.TenantEntity;
import com.tandiantong.security.entity.TenantPaymentConfigEntity;
import com.tandiantong.security.mapper.AdminUserMapper;
import com.tandiantong.security.mapper.MerchantInvitationMapper;
import com.tandiantong.security.mapper.MiniProgramSceneMapper;
import com.tandiantong.security.mapper.StoreMapper;
import com.tandiantong.security.mapper.TenantMapper;
import com.tandiantong.security.mapper.TenantPaymentConfigMapper;
import com.tandiantong.security.audit.OperationAuditService;
import com.tandiantong.security.context.CurrentUser;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商户开通持久化服务，负责平台创建商户、激活邀请和查看商户列表。
 */
@Service
public class MerchantProvisioningService {

    private static final String ENABLED_STATUS = "ENABLED";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    private final TenantMapper tenantMapper;
    private final StoreMapper storeMapper;
    private final MerchantInvitationMapper merchantInvitationMapper;
    private final TenantPaymentConfigMapper tenantPaymentConfigMapper;
    private final MiniProgramSceneMapper miniProgramSceneMapper;
    private final AdminUserMapper adminUserMapper;
    private final OperationAuditService auditService;
    private final PasswordService passwordService = new PasswordService();

    public MerchantProvisioningService(TenantMapper tenantMapper, StoreMapper storeMapper,
                                       MerchantInvitationMapper merchantInvitationMapper,
                                       TenantPaymentConfigMapper tenantPaymentConfigMapper,
                                       MiniProgramSceneMapper miniProgramSceneMapper, AdminUserMapper adminUserMapper,
                                       OperationAuditService auditService) {
        this.tenantMapper = tenantMapper;
        this.storeMapper = storeMapper;
        this.merchantInvitationMapper = merchantInvitationMapper;
        this.tenantPaymentConfigMapper = tenantPaymentConfigMapper;
        this.miniProgramSceneMapper = miniProgramSceneMapper;
        this.adminUserMapper = adminUserMapper;
        this.auditService = auditService;
    }

    /**
     * 创建待启用商户，并生成首个管理员邀请和小程序入口码。
     */
    @Transactional
    public ProvisionedMerchant provision(MerchantOnboardingCommand command) {
        validate(command);
        TenantEntity tenant = insertTenant(command.merchantName());
        StoreEntity store = insertStore(tenant.getId(), command.merchantName() + "默认门店");
        String invitationCode = randomCode("invite");
        String sceneKey = randomCode("scene");
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        insertInvitation(tenant.getId(), store.getId(), command, invitationCode, expiresAt);
        insertPaymentConfig(tenant.getId(), store.getId());
        insertMiniProgramScene(tenant.getId(), store.getId(), sceneKey);
        return new ProvisionedMerchant(tenant.getId(), store.getId(), command.merchantName(), command.merchantName() + "默认门店",
                invitationCode, expiresAt, sceneKey, PaymentConfigStatus.NOT_CONFIGURED);
    }

    /**
     * 激活商户管理员邀请。
     */
    @Transactional
    public void activateInvitation(String invitationCode, String password) {
        if (invitationCode == null || invitationCode.isBlank() || password == null || password.length() < 8) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "邀请码或密码不符合要求");
        }
        MerchantInvitationEntity invitation = merchantInvitationMapper.selectOne(new QueryWrapper<MerchantInvitationEntity>()
                .eq("invitation_code_hash", sha256(invitationCode))
                .last("for update"));
        if (invitation == null || invitation.getUsedAt() != null || toInstant(invitation.getExpiresAt()).isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "邀请码无效或已过期");
        }
        AdminUserEntity adminUser = new AdminUserEntity();
        adminUser.setId(System.currentTimeMillis());
        adminUser.setTenantId(invitation.getTenantId());
        adminUser.setStoreId(invitation.getStoreId());
        adminUser.setMobile(invitation.getAdminMobile());
        adminUser.setDisplayName(invitation.getAdminName());
        adminUser.setPasswordHash(passwordService.hash(password));
        adminUser.setStatus(ENABLED_STATUS);
        adminUser.setTokenVersion(1);
        adminUserMapper.insert(adminUser);

        invitation.setUsedAt(LocalDateTime.now(BUSINESS_ZONE));
        merchantInvitationMapper.updateById(invitation);
    }

    /**
     * 启用商户，启用后才允许产生新的业务写操作。
     */
    @Transactional
    public void enableMerchant(Long tenantId) {
        requireTenant(tenantId);
        tenantMapper.update(null, new LambdaUpdateWrapper<TenantEntity>()
                .eq(TenantEntity::getId, tenantId)
                .ne(TenantEntity::getStatus, TenantStatus.ENABLED.name())
                .set(TenantEntity::getStatus, TenantStatus.ENABLED.name()));
    }

    /**
     * 停用商户，后续商户登录和业务请求会被租户状态校验拦截。
     */
    @Transactional
    public void disableMerchant(CurrentUser operator, Long tenantId) {
        requireTenant(tenantId);
        tenantMapper.update(null, new LambdaUpdateWrapper<TenantEntity>()
                .eq(TenantEntity::getId, tenantId)
                .ne(TenantEntity::getStatus, TenantStatus.DISABLED.name())
                .set(TenantEntity::getStatus, TenantStatus.DISABLED.name()));
        auditService.record(operator, "停用商户", "商户租户", tenantId.toString(), "停用商户后台访问和业务写操作");
    }

    /**
     * 重新生成未激活商户的管理员邀请码，旧邀请码全部立即失效。
     */
    @Transactional
    public ReissuedInvitation reissueInvitation(CurrentUser operator, Long tenantId) {
        TenantEntity tenant = requireTenant(tenantId);
        Long activeAdminCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getTenantId, tenantId)
                .eq(AdminUserEntity::getStatus, ENABLED_STATUS));
        if (activeAdminCount > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商户管理员已激活，不能重新生成邀请码");
        }
        MerchantInvitationEntity latestInvitation = merchantInvitationMapper.selectOne(new LambdaQueryWrapper<MerchantInvitationEntity>()
                .eq(MerchantInvitationEntity::getTenantId, tenantId)
                .orderByDesc(MerchantInvitationEntity::getId)
                .last("limit 1"));
        if (latestInvitation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户管理员邀请不存在");
        }

        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        merchantInvitationMapper.update(null, new LambdaUpdateWrapper<MerchantInvitationEntity>()
                .eq(MerchantInvitationEntity::getTenantId, tenantId)
                .isNull(MerchantInvitationEntity::getUsedAt)
                .set(MerchantInvitationEntity::getExpiresAt, now.minusSeconds(1)));
        String invitationCode = randomCode("invite");
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        insertInvitation(tenantId, latestInvitation.getStoreId(), latestInvitation.getAdminName(),
                latestInvitation.getAdminMobile(), invitationCode, expiresAt);
        auditService.record(operator, "重新生成商户邀请码", "商户租户", tenantId.toString(),
                "商户：" + tenant.getName() + "，旧邀请码已失效");
        return new ReissuedInvitation(invitationCode, expiresAt);
    }

    /**
     * 查询平台商户列表。
     */
    public List<MerchantOverview> listMerchants() {
        return tenantMapper.selectList(new LambdaQueryWrapper<TenantEntity>()
                        .orderByDesc(TenantEntity::getId)
                        .last("limit 200"))
                .stream()
                .map(this::toOverview)
                .toList();
    }

    private TenantEntity insertTenant(String merchantName) {
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantCode("T" + System.currentTimeMillis());
        tenant.setName(merchantName);
        tenant.setStatus(TenantStatus.PENDING_ENABLE.name());
        tenantMapper.insert(tenant);
        return tenant;
    }

    private StoreEntity insertStore(Long tenantId, String storeName) {
        StoreEntity store = new StoreEntity();
        store.setTenantId(tenantId);
        store.setName(storeName);
        store.setStatus(ENABLED_STATUS);
        storeMapper.insert(store);
        return store;
    }

    private void insertInvitation(Long tenantId, Long storeId, MerchantOnboardingCommand command,
                                  String invitationCode, Instant expiresAt) {
        insertInvitation(tenantId, storeId, command.adminName(), command.adminMobile(), invitationCode, expiresAt);
    }

    private void insertInvitation(Long tenantId, Long storeId, String adminName, String adminMobile,
                                  String invitationCode, Instant expiresAt) {
        MerchantInvitationEntity invitation = new MerchantInvitationEntity();
        invitation.setTenantId(tenantId);
        invitation.setStoreId(storeId);
        invitation.setAdminName(adminName);
        invitation.setAdminMobile(adminMobile);
        invitation.setInvitationCodeHash(sha256(invitationCode));
        invitation.setExpiresAt(LocalDateTime.ofInstant(expiresAt, BUSINESS_ZONE));
        merchantInvitationMapper.insert(invitation);
    }

    private void insertPaymentConfig(Long tenantId, Long storeId) {
        TenantPaymentConfigEntity paymentConfig = new TenantPaymentConfigEntity();
        paymentConfig.setTenantId(tenantId);
        paymentConfig.setStoreId(storeId);
        paymentConfig.setStatus(PaymentConfigStatus.NOT_CONFIGURED.name());
        tenantPaymentConfigMapper.insert(paymentConfig);
    }

    private void insertMiniProgramScene(Long tenantId, Long storeId, String sceneKey) {
        MiniProgramSceneEntity scene = new MiniProgramSceneEntity();
        scene.setTenantId(tenantId);
        scene.setStoreId(storeId);
        scene.setSceneKey(sceneKey);
        scene.setEnabled(true);
        miniProgramSceneMapper.insert(scene);
    }

    private MerchantOverview toOverview(TenantEntity tenant) {
        MerchantInvitationEntity invitation = merchantInvitationMapper.selectOne(new LambdaQueryWrapper<MerchantInvitationEntity>()
                .eq(MerchantInvitationEntity::getTenantId, tenant.getId())
                .orderByDesc(MerchantInvitationEntity::getId)
                .last("limit 1"));
        TenantPaymentConfigEntity paymentConfig = tenantPaymentConfigMapper.selectOne(new LambdaQueryWrapper<TenantPaymentConfigEntity>()
                .eq(TenantPaymentConfigEntity::getTenantId, tenant.getId())
                .last("limit 1"));
        MiniProgramSceneEntity scene = miniProgramSceneMapper.selectOne(new LambdaQueryWrapper<MiniProgramSceneEntity>()
                .eq(MiniProgramSceneEntity::getTenantId, tenant.getId())
                .last("limit 1"));
        Long activeAdminCount = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getTenantId, tenant.getId())
                .eq(AdminUserEntity::getStatus, ENABLED_STATUS));
        return new MerchantOverview(tenant.getId(), tenant.getName(),
                invitation == null ? "" : invitation.getAdminName(),
                maskMobile(invitation == null ? null : invitation.getAdminMobile()),
                tenant.getStatus(),
                paymentConfig == null ? PaymentConfigStatus.NOT_CONFIGURED.name() : paymentConfig.getStatus(),
                activeAdminCount > 0 ? "ACTIVATED" : "PENDING",
                scene == null ? "" : scene.getSceneKey());
    }

    private TenantEntity requireTenant(Long tenantId) {
        TenantEntity tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户不存在");
        }
        return tenant;
    }

    private void validate(MerchantOnboardingCommand command) {
        if (command.merchantName() == null || command.merchantName().isBlank()
                || command.adminName() == null || command.adminName().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商户名称和管理员姓名不能为空");
        }
        if (command.adminMobile() == null || !command.adminMobile().matches("1\\d{10}")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "管理员手机号格式不正确");
        }
    }

    private String randomCode(String prefix) {
        return prefix + "-" + java.util.UUID.randomUUID().toString().replace("-", "");
    }

    private String sha256(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境缺少 SHA-256 摘要算法", exception);
        }
    }

    private String maskMobile(String mobile) {
        if (mobile == null || !mobile.matches("1\\d{10}")) {
            return "****";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime.atZone(BUSINESS_ZONE).toInstant();
    }

    public record ProvisionedMerchant(Long tenantId, Long storeId, String merchantName, String storeName,
                                      String invitationCode, Instant invitationExpiresAt, String sceneKey,
                                      PaymentConfigStatus paymentConfigStatus) {
    }

    public record MerchantOverview(Long tenantId, String merchantName, String adminName, String adminMobileMasked,
                                   String status, String paymentConfigStatus, String adminStatus, String sceneKey) {
    }

    /** 商户邀请码重置结果，仅在生成时返回明文邀请码。 */
    public static class ReissuedInvitation {
        private final String invitationCode;
        private final Instant invitationExpiresAt;

        public ReissuedInvitation(String invitationCode, Instant invitationExpiresAt) {
            this.invitationCode = invitationCode;
            this.invitationExpiresAt = invitationExpiresAt;
        }

        public String getInvitationCode() {
            return invitationCode;
        }

        public Instant getInvitationExpiresAt() {
            return invitationExpiresAt;
        }
    }
}
