package com.tandiantong.security.tenant;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantProvisioningService {

    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    public MerchantProvisioningService(ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    @Transactional
    public ProvisionedMerchant provision(MerchantOnboardingCommand command) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("当前运行环境未配置数据库连接");
        }
        validate(command);
        Long tenantId = insertTenant(jdbcTemplate, command.merchantName());
        Long storeId = insertStore(jdbcTemplate, tenantId, command.merchantName() + "默认门店");
        String invitationCode = randomCode("invite");
        String sceneKey = randomCode("scene");
        Instant expiresAt = Instant.now().plus(7, ChronoUnit.DAYS);
        insertInvitation(jdbcTemplate, tenantId, storeId, command, invitationCode, expiresAt);
        jdbcTemplate.update("insert into tenant_payment_config (tenant_id, store_id, status) values (?, ?, ?)",
                tenantId, storeId, PaymentConfigStatus.NOT_CONFIGURED.name());
        jdbcTemplate.update("insert into mini_program_scene (tenant_id, store_id, scene_key, enabled) values (?, ?, ?, true)",
                tenantId, storeId, sceneKey);
        return new ProvisionedMerchant(tenantId, storeId, command.merchantName(), command.merchantName() + "默认门店",
                invitationCode, expiresAt, sceneKey, PaymentConfigStatus.NOT_CONFIGURED);
    }

    private Long insertTenant(JdbcTemplate jdbcTemplate, String merchantName) {
        return insertAndReturnId(jdbcTemplate, "insert into tenant (tenant_code, name, status) values (?, ?, ?)",
                "T" + System.currentTimeMillis(), merchantName, TenantStatus.PENDING_ENABLE.name());
    }

    private Long insertStore(JdbcTemplate jdbcTemplate, Long tenantId, String storeName) {
        return insertAndReturnId(jdbcTemplate, "insert into store (tenant_id, name, status) values (?, ?, ?)",
                tenantId, storeName, "ENABLED");
    }

    private void insertInvitation(JdbcTemplate jdbcTemplate, Long tenantId, Long storeId, MerchantOnboardingCommand command,
                                  String invitationCode, Instant expiresAt) {
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "insert into merchant_invitation (tenant_id, store_id, admin_name, admin_mobile, invitation_code_hash, expires_at) values (?, ?, ?, ?, ?, ?)");
            statement.setLong(1, tenantId);
            statement.setLong(2, storeId);
            statement.setString(3, command.adminName());
            statement.setString(4, command.adminMobile());
            statement.setString(5, sha256(invitationCode));
            statement.setObject(6, expiresAt);
            return statement;
        });
    }

    private Long insertAndReturnId(JdbcTemplate jdbcTemplate, String sql, Object... values) {
        return jdbcTemplate.execute((ConnectionCallback<Long>) connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("商户开通未返回数据库主键");
                }
                return keys.getLong(1);
            }
        });
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

    public record ProvisionedMerchant(Long tenantId, Long storeId, String merchantName, String storeName,
                                      String invitationCode, Instant invitationExpiresAt, String sceneKey,
                                      PaymentConfigStatus paymentConfigStatus) {
    }
}
