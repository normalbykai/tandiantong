package com.tandiantong.security.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.security.audit.OperationAuditService;
import com.tandiantong.security.audit.AuditAction;
import com.tandiantong.security.audit.AuditEvent;
import com.tandiantong.security.audit.AuditTarget;
import com.tandiantong.security.auth.PasswordService;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.PlatformDictionaryItemEntity;
import com.tandiantong.security.entity.PlatformDictionaryTypeEntity;
import com.tandiantong.security.entity.PlatformSystemConfigEntity;
import com.tandiantong.security.mapper.PlatformDictionaryItemMapper;
import com.tandiantong.security.mapper.PlatformDictionaryTypeMapper;
import com.tandiantong.security.mapper.PlatformSystemConfigMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/** 平台系统配置和平台字典管理服务。 */
@Service
@ConditionalOnProperty(
        prefix = "tandiantong.security",
        name = "database-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PlatformSystemManagementService {
    private static final long CONFIG_ID = 1L;
    private static final String ENABLED = "ENABLED";
    private static final String DISABLED = "DISABLED";
    private static final String RANDOM = "RANDOM";
    private static final String FIXED = "FIXED";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_INFO = "info";
    private static final String TAG_DANGER = "danger";
    private static final String TAG_WARNING = "warning";
    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";
    private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();
    private final PlatformSystemConfigMapper configMapper;
    private final PlatformDictionaryItemMapper dictionaryMapper;
    private final PlatformDictionaryTypeMapper dictionaryTypeMapper;
    private final OperationAuditService auditService;
    private final PasswordService passwordService;

    public PlatformSystemManagementService(
            PlatformSystemConfigMapper configMapper,
            PlatformDictionaryItemMapper dictionaryMapper,
            PlatformDictionaryTypeMapper dictionaryTypeMapper,
            OperationAuditService auditService,
            PasswordService passwordService) {
        this.configMapper = configMapper;
        this.dictionaryMapper = dictionaryMapper;
        this.dictionaryTypeMapper = dictionaryTypeMapper;
        this.auditService = auditService;
        this.passwordService = passwordService;
    }

    public PlatformSystemConfigEntity getConfig() {
        PlatformSystemConfigEntity config = configMapper.selectById(CONFIG_ID);
        if (config == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "平台系统配置不存在");
        return config;
    }

    @Transactional
    public PlatformSystemConfigEntity updateConfig(
            CurrentUser operator,
            String logoUrl,
            String description,
            String resetPasswordMode,
            String fixedResetPassword) {
        PlatformSystemConfigEntity config = getConfig();
        validatePasswordPolicy(config, resetPasswordMode, fixedResetPassword);
        config.setLogoUrl(logoUrl);
        config.setDescription(description);
        config.setResetPasswordMode(resetPasswordMode);
        if (FIXED.equals(resetPasswordMode)
                && fixedResetPassword != null
                && !fixedResetPassword.isBlank()) {
            config.setFixedResetPasswordHash(passwordService.hash(fixedResetPassword));
        } else if (RANDOM.equals(resetPasswordMode)) {
            config.setFixedResetPasswordHash(null);
        }
        config.setUpdatedBy(operator.userId());
        configMapper.updateById(config);
        auditService.record(
                operator,
                AuditEvent.of(
                        AuditAction.PLATFORM_SYSTEM_CONFIG_UPDATED,
                        AuditTarget.of("平台系统配置", CONFIG_ID, "平台系统配置")));
        return config;
    }

    /** 按平台策略生成或取得临时密码；固定密码只返回哈希，避免在接口中回显。 */
    public TemporaryPassword resolveResetPassword() {
        PlatformSystemConfigEntity config = getConfig();
        String mode =
                config.getResetPasswordMode() == null ? RANDOM : config.getResetPasswordMode();
        if (FIXED.equals(mode)) {
            if (config.getFixedResetPasswordHash() == null) throw error("固定临时密码尚未配置");
            return new TemporaryPassword(null, mode, config.getFixedResetPasswordHash());
        }
        String password = randomPassword();
        return new TemporaryPassword(password, RANDOM, passwordService.hash(password));
    }

    private void validatePasswordPolicy(
            PlatformSystemConfigEntity config, String mode, String fixedPassword) {
        if (!RANDOM.equals(mode) && !FIXED.equals(mode)) throw error("重置密码策略不正确");
        if (FIXED.equals(mode)
                && (fixedPassword == null || fixedPassword.isBlank())
                && config.getFixedResetPasswordHash() == null) {
            throw error("固定策略必须配置临时密码");
        }
        if (fixedPassword != null
                && !fixedPassword.isBlank()
                && (fixedPassword.length() < 8 || fixedPassword.length() > 64)) {
            throw error("固定临时密码长度需为8至64位");
        }
    }

    private String randomPassword() {
        StringBuilder password = new StringBuilder(16);
        for (int i = 0; i < 16; i++)
            password.append(
                    PASSWORD_ALPHABET.charAt(RANDOM_GENERATOR.nextInt(PASSWORD_ALPHABET.length())));
        return password.toString();
    }

    public static class TemporaryPassword {
        private final String plainPassword;
        private final String mode;
        private final String passwordHash;

        public TemporaryPassword(String plainPassword, String mode, String passwordHash) {
            this.plainPassword = plainPassword;
            this.mode = mode;
            this.passwordHash = passwordHash;
        }

        public String plainPassword() {
            return plainPassword;
        }

        public String mode() {
            return mode;
        }

        public String passwordHash() {
            return passwordHash;
        }
    }

    /** 查询全部已启用的字典类型，按排序值排列。 */
    public List<PlatformDictionaryTypeEntity> listDictionaryTypes() {
        return dictionaryTypeMapper.selectList(
                new LambdaQueryWrapper<PlatformDictionaryTypeEntity>()
                        .eq(PlatformDictionaryTypeEntity::getStatus, ENABLED)
                        .orderByAsc(PlatformDictionaryTypeEntity::getSortOrder));
    }

    public List<PlatformDictionaryItemEntity> listDictionaryItems(String dictionaryType) {
        LambdaQueryWrapper<PlatformDictionaryItemEntity> query =
                new LambdaQueryWrapper<PlatformDictionaryItemEntity>()
                        .orderByAsc(PlatformDictionaryItemEntity::getDictionaryType)
                        .orderByAsc(PlatformDictionaryItemEntity::getSortOrder)
                        .orderByAsc(PlatformDictionaryItemEntity::getId);
        if (dictionaryType != null && !dictionaryType.isBlank())
            query.eq(PlatformDictionaryItemEntity::getDictionaryType, dictionaryType);
        return dictionaryMapper.selectList(query);
    }

    @Transactional
    public PlatformDictionaryItemEntity createDictionaryItem(
            CurrentUser operator,
            String dictionaryType,
            String itemCode,
            String itemLabel,
            String tagType,
            Integer sortOrder) {
        if (dictionaryMapper.selectOne(dictionaryQuery(dictionaryType, itemCode)) != null)
            throw error("字典项编码已存在");
        PlatformDictionaryItemEntity item = new PlatformDictionaryItemEntity();
        item.setDictionaryType(dictionaryType);
        item.setItemCode(itemCode);
        item.setItemLabel(itemLabel);
        item.setTagType(resolveTagType(tagType, itemCode));
        item.setSortOrder(sortOrder);
        item.setStatus(ENABLED);
        dictionaryMapper.insert(item);
        auditService.record(
                operator,
                AuditEvent.of(
                        AuditAction.PLATFORM_DICTIONARY_ITEM_CREATED,
                        AuditTarget.of("平台字典项", item.getId(), itemLabel, dictionaryType + "/" + itemCode)));
        return item;
    }

    @Transactional
    public void updateDictionaryItem(
            CurrentUser operator, Long id, String itemLabel, String tagType, Integer sortOrder) {
        PlatformDictionaryItemEntity item = requireDictionaryItem(id);
        item.setItemLabel(itemLabel);
        item.setTagType(resolveTagType(tagType, item.getItemCode()));
        item.setSortOrder(sortOrder);
        dictionaryMapper.updateById(item);
        auditService.record(
                operator,
                AuditEvent.of(
                        AuditAction.PLATFORM_DICTIONARY_ITEM_UPDATED,
                        AuditTarget.of("平台字典项", id, item.getItemLabel(), item.getDictionaryType() + "/" + item.getItemCode())));
    }

    @Transactional
    public void updateDictionaryStatus(CurrentUser operator, Long id, boolean enabled) {
        PlatformDictionaryItemEntity item = requireDictionaryItem(id);
        item.setStatus(enabled ? ENABLED : DISABLED);
        dictionaryMapper.updateById(item);
        auditService.record(
                operator,
                AuditEvent.of(
                        enabled ? AuditAction.PLATFORM_DICTIONARY_ITEM_ENABLED : AuditAction.PLATFORM_DICTIONARY_ITEM_DISABLED,
                        AuditTarget.of("平台字典项", id, item.getItemLabel(), item.getDictionaryType() + "/" + item.getItemCode())));
    }

    private LambdaQueryWrapper<PlatformDictionaryItemEntity> dictionaryQuery(
            String type, String code) {
        return new LambdaQueryWrapper<PlatformDictionaryItemEntity>()
                .eq(PlatformDictionaryItemEntity::getDictionaryType, type)
                .eq(PlatformDictionaryItemEntity::getItemCode, code);
    }

    private PlatformDictionaryItemEntity requireDictionaryItem(Long id) {
        PlatformDictionaryItemEntity item = dictionaryMapper.selectById(id);
        if (item == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "平台字典项不存在");
        return item;
    }

    private String resolveTagType(String tagType, String itemCode) {
        if (tagType != null && !tagType.isBlank()) {
            if (TAG_SUCCESS.equals(tagType)
                    || TAG_INFO.equals(tagType)
                    || TAG_DANGER.equals(tagType)
                    || TAG_WARNING.equals(tagType)) {
                return tagType;
            }
            throw error("字典项标签颜色类型不正确");
        }
        if (itemCode == null || itemCode.isBlank()) {
            return TAG_INFO;
        }
        if (itemCode.equals("ENABLED")
                || itemCode.equals("SUCCESS")
                || itemCode.equals("VERIFIED")
                || itemCode.equals("COMPLETED")
                || itemCode.equals("FULFILLED")
                || itemCode.equals("ON_SHELF")) {
            return TAG_SUCCESS;
        }
        if (itemCode.equals("FAILED") || itemCode.equals("VERIFY_FAILED") || itemCode.equals("CANCELED")) {
            return TAG_DANGER;
        }
        if (itemCode.equals("PROCESSING")
                || itemCode.equals("REFUNDING")
                || itemCode.equals("PENDING_PAYMENT")
                || itemCode.equals("PENDING_VERIFY")
                || itemCode.equals("PENDING_REVIEW")
                || itemCode.equals("PENDING_ENABLE")
                || itemCode.equals("PENDING")) {
            return TAG_WARNING;
        }
        return TAG_INFO;
    }

    private BusinessException error(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
