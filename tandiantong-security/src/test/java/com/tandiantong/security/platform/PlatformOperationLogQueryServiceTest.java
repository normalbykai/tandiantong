package com.tandiantong.security.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.entity.OperationLogEntity;
import com.tandiantong.security.entity.PlatformUserEntity;
import com.tandiantong.security.mapper.OperationLogMapper;
import com.tandiantong.security.mapper.PlatformUserMapper;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class PlatformOperationLogQueryServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnPlatformLogsWithOperatorInfo() {
        OperationLogMapper operationLogMapper = mock(OperationLogMapper.class);
        PlatformUserMapper platformUserMapper = mock(PlatformUserMapper.class);

        OperationLogEntity log = new OperationLogEntity();
        log.setId(1L);
        log.setTenantId(null);
        log.setDomain(AccessDomain.PLATFORM.name());
        log.setOperatorId(10L);
        log.setOperationType("更新平台系统配置");
        log.setTargetType("平台系统配置");
        log.setTargetId("1");
        log.setDetail("更新平台 Logo 与描述信息");
        log.setTraceId("trace-001");
        log.setUserIp("127.0.0.1");
        log.setRequestMethod("PUT");
        log.setRequestUrl("/api/platform/v1/system/config");
        log.setCreatedAt(LocalDateTime.of(2026, 7, 21, 10, 15));

        Page<OperationLogEntity> page = new Page<>(1, 20);
        page.setTotal(1);
        page.setRecords(List.of(log));
        when(operationLogMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PlatformUserEntity operator = new PlatformUserEntity();
        operator.setId(10L);
        operator.setMobile("13900000000");
        operator.setDisplayName("系统管理员");
        when(platformUserMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(operator));

        PlatformOperationLogQueryService service =
                new PlatformOperationLogQueryService(operationLogMapper, platformUserMapper);

        PlatformOperationLogQueryService.PlatformOperationLogPage result =
                service.listPlatformLogs(null, null, null, null, null, null, 1, 20);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.current()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().get(0).getOperatorName()).isEqualTo("系统管理员");
        assertThat(result.records().get(0).getRequestUrl()).isEqualTo("/api/platform/v1/system/config");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMarkDisableAndDeleteLogsAsSensitive() {
        OperationLogMapper operationLogMapper = mock(OperationLogMapper.class);
        PlatformUserMapper platformUserMapper = mock(PlatformUserMapper.class);

        OperationLogEntity log = new OperationLogEntity();
        log.setId(2L);
        log.setDomain(AccessDomain.PLATFORM.name());
        log.setOperationType("删除平台账号");
        log.setTargetType("平台账号");

        Page<OperationLogEntity> page = new Page<>(1, 20);
        page.setTotal(1);
        page.setRecords(List.of(log));
        when(operationLogMapper.selectPage(any(IPage.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(platformUserMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        PlatformOperationLogQueryService service =
                new PlatformOperationLogQueryService(operationLogMapper, platformUserMapper);

        PlatformOperationLogQueryService.PlatformOperationLogPage result =
                service.listPlatformLogs(null, null, null, null, null, null, 1, 20);

        assertThat(result.records().get(0).isSensitive()).isTrue();
    }
}
