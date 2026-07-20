package com.tandiantong.security.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tandiantong.framework.operatelog.core.model.OperationLogCommand;
import com.tandiantong.framework.operatelog.core.service.OperationLogRequestEnricher;
import com.tandiantong.security.entity.OperationLogEntity;
import com.tandiantong.security.mapper.OperationLogMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

class DatabaseOperationAuditServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldPersistRequestContextAfterEnriched() {
        OperationLogMapper mapper = mock(OperationLogMapper.class);
        when(mapper.insert(any(OperationLogEntity.class))).thenReturn(1);
        OperationLogRequestEnricher enricher = mock(OperationLogRequestEnricher.class);
        doAnswer(invocation -> {
            OperationLogCommand enrichedCommand = invocation.getArgument(0);
            enrichedCommand.setUserIp("203.0.113.10");
            enrichedCommand.setUserAgent("Mozilla/5.0");
            enrichedCommand.setRequestMethod("POST");
            enrichedCommand.setRequestUrl("/api/admin/v1/orders/refund");
            return null;
        }).when(enricher).enrich(any(OperationLogCommand.class));
        ObjectProvider<OperationLogRequestEnricher> requestEnricherProvider = mock(ObjectProvider.class);
        when(requestEnricherProvider.getIfAvailable()).thenReturn(enricher);
        DatabaseOperationAuditService service = new DatabaseOperationAuditService(
                mapper,
                requestEnricherProvider);
        OperationLogCommand command = new OperationLogCommand();
        command.setTenantId(100L);
        command.setStoreId(1000L);
        command.setDomain("TENANT");
        command.setOperatorId(10L);
        command.setOperationType("订单退款");
        command.setTargetType("商品订单");
        command.setTargetId("SO202607200001");
        command.setDetail("已提交核销前整单退款申请");
        command.setTraceId("trace-001");

        service.record(command);

        ArgumentCaptor<OperationLogEntity> captor = ArgumentCaptor.forClass(OperationLogEntity.class);
        verify(mapper).insert(captor.capture());
        OperationLogEntity entity = captor.getValue();
        assertThat(entity.getTenantId()).isEqualTo(100L);
        assertThat(entity.getTraceId()).isEqualTo("trace-001");
        assertThat(entity.getUserIp()).isEqualTo("203.0.113.10");
        assertThat(entity.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(entity.getRequestMethod()).isEqualTo("POST");
        assertThat(entity.getRequestUrl()).isEqualTo("/api/admin/v1/orders/refund");
    }
}
