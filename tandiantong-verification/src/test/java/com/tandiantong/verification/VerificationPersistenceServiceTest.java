package com.tandiantong.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tandiantong.verification.app.VerificationBusinessCompletionHandler;
import com.tandiantong.verification.app.VerificationPersistenceService;
import com.tandiantong.verification.entity.VerificationCredentialEntity;
import com.tandiantong.verification.entity.VerificationRecordEntity;
import com.tandiantong.verification.mapper.PickupNoSequenceMapper;
import com.tandiantong.verification.mapper.VerificationCredentialMapper;
import com.tandiantong.verification.mapper.VerificationRecordMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * 核销持久化服务测试，覆盖首次核销和重复核销幂等行为。
 */
class VerificationPersistenceServiceTest {

    private VerificationCredentialMapper credentialMapper;
    private VerificationRecordMapper recordMapper;
    private VerificationBusinessCompletionHandler completionHandler;
    private VerificationPersistenceService service;

    @BeforeEach
    void setUp() {
        credentialMapper = Mockito.mock(VerificationCredentialMapper.class);
        recordMapper = Mockito.mock(VerificationRecordMapper.class);
        completionHandler = Mockito.mock(VerificationBusinessCompletionHandler.class);
        when(completionHandler.supports("PRODUCT_ORDER")).thenReturn(true);
        service = new VerificationPersistenceService(Mockito.mock(PickupNoSequenceMapper.class),
                credentialMapper, recordMapper, List.of(completionHandler));
    }

    @Test
    void shouldRecordAndCompleteBusinessOnlyOnFirstVerification() {
        VerificationCredentialEntity credential = credential("PENDING");
        when(credentialMapper.selectOne(any())).thenReturn(credential);
        when(credentialMapper.updateStatusByToken(any(), any(), anyString(), anyString(), anyString())).thenReturn(1);

        VerificationPersistenceService.VerificationResult result = service.verify(1L, 2L, 9L, "vk-test", null);

        assertThat(result.status()).isEqualTo("VERIFIED");
        verify(recordMapper).insert(any(VerificationRecordEntity.class));
        verify(completionHandler).complete(1L, 2L, "SO1001");
    }

    @Test
    void shouldNotRepeatSideEffectsForVerifiedCredential() {
        VerificationCredentialEntity credential = credential("VERIFIED");
        when(credentialMapper.selectOne(any())).thenReturn(credential);
        when(credentialMapper.updateStatusByToken(any(), any(), anyString(), anyString(), anyString())).thenReturn(0);

        VerificationPersistenceService.VerificationResult result = service.verify(1L, 2L, 9L, "vk-test", null);

        assertThat(result.status()).isEqualTo("VERIFIED");
        verify(recordMapper, never()).insert(any(VerificationRecordEntity.class));
        verify(completionHandler, never()).complete(any(), any(), anyString());
    }

    private VerificationCredentialEntity credential(String status) {
        VerificationCredentialEntity credential = new VerificationCredentialEntity();
        credential.setTenantId(1L);
        credential.setStoreId(2L);
        credential.setBusinessType("PRODUCT_ORDER");
        credential.setBusinessNo("SO1001");
        credential.setSummary("商品订单 SO1001");
        credential.setPickupNo("A001");
        credential.setStatus(status);
        return credential;
    }
}
