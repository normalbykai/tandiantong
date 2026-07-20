package com.tandiantong.security.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tandiantong.security.audit.OperationAuditService;
import com.tandiantong.security.auth.PasswordService;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.PlatformDictionaryItemEntity;
import com.tandiantong.security.entity.PlatformSystemConfigEntity;
import com.tandiantong.security.mapper.PlatformDictionaryItemMapper;
import com.tandiantong.security.mapper.PlatformDictionaryTypeMapper;
import com.tandiantong.security.mapper.PlatformSystemConfigMapper;
import org.junit.jupiter.api.Test;

class PlatformSystemManagementServiceTest {

    @Test
    void randomPolicyShouldGenerateDifferentTemporaryPasswordAndHash() {
        PlatformSystemConfigMapper configMapper = mock(PlatformSystemConfigMapper.class);
        PlatformSystemConfigEntity config = config("RANDOM", null);
        when(configMapper.selectById(1L)).thenReturn(config);
        PlatformSystemManagementService service = service(configMapper);

        PlatformSystemManagementService.TemporaryPassword result = service.resolveResetPassword();

        assertThat(result.mode()).isEqualTo("RANDOM");
        assertThat(result.plainPassword()).hasSize(16);
        assertThat(result.passwordHash()).isNotEqualTo(result.plainPassword());
        assertThat(new PasswordService().matches(result.plainPassword(), result.passwordHash())).isTrue();
    }

    @Test
    void fixedPolicyShouldNeverReturnPlainPassword() {
        PlatformSystemConfigMapper configMapper = mock(PlatformSystemConfigMapper.class);
        PlatformSystemConfigEntity config = config("FIXED", new PasswordService().hash("Temp@123456"));
        when(configMapper.selectById(1L)).thenReturn(config);
        PlatformSystemManagementService.TemporaryPassword result = service(configMapper).resolveResetPassword();

        assertThat(result.mode()).isEqualTo("FIXED");
        assertThat(result.plainPassword()).isNull();
        assertThat(new PasswordService().matches("Temp@123456", result.passwordHash())).isTrue();
    }

    @Test
    void createDictionaryItemShouldKeepBusinessStorageValue() {
        PlatformDictionaryItemMapper dictionaryMapper = mock(PlatformDictionaryItemMapper.class);
        doAnswer(invocation -> {
            PlatformDictionaryItemEntity item = invocation.getArgument(0);
            item.setId(1L);
            return 1;
        }).when(dictionaryMapper).insert(any(PlatformDictionaryItemEntity.class));
        PlatformSystemManagementService service = new PlatformSystemManagementService(
                mock(PlatformSystemConfigMapper.class),
                dictionaryMapper,
                mock(PlatformDictionaryTypeMapper.class),
                mock(OperationAuditService.class),
                new PasswordService());

        PlatformDictionaryItemEntity item = service.createDictionaryItem(
                CurrentUser.platform(1L, "demo@example.com", "平台管理员"), "ORDER_STATUS", "PENDING", "pending", "待支付", 10);

        assertThat(item.getItemCode()).isEqualTo("PENDING");
        assertThat(item.getItemValue()).isEqualTo("pending");
        assertThat(item.getItemLabel()).isEqualTo("待支付");
    }

    private PlatformSystemManagementService service(PlatformSystemConfigMapper configMapper) {
        return new PlatformSystemManagementService(
                configMapper,
                mock(PlatformDictionaryItemMapper.class),
                mock(PlatformDictionaryTypeMapper.class),
                mock(OperationAuditService.class),
                new PasswordService());
    }

    private PlatformSystemConfigEntity config(String mode, String hash) {
        PlatformSystemConfigEntity config = new PlatformSystemConfigEntity();
        config.setId(1L);
        config.setResetPasswordMode(mode);
        config.setFixedResetPasswordHash(hash);
        return config;
    }
}
