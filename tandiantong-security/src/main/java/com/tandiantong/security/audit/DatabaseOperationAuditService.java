package com.tandiantong.security.audit;

import com.tandiantong.framework.operatelog.core.model.OperationLogCommand;
import com.tandiantong.framework.operatelog.core.service.OperationLogRequestEnricher;
import com.tandiantong.security.entity.OperationLogEntity;
import com.tandiantong.security.mapper.OperationLogMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** 数据库关键后台操作审计服务。 */
@Service
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseOperationAuditService implements OperationAuditService {

    private final OperationLogMapper operationLogMapper;

    private final ObjectProvider<OperationLogRequestEnricher> requestEnricher;

    public DatabaseOperationAuditService(
            OperationLogMapper operationLogMapper,
            ObjectProvider<OperationLogRequestEnricher> requestEnricher) {
        this.operationLogMapper = operationLogMapper;
        this.requestEnricher = requestEnricher;
    }

    /** 将关键操作写入独立审计日志表。 */
    @Override
    public void record(OperationLogCommand command) {
        OperationLogRequestEnricher enricher = requestEnricher.getIfAvailable();
        if (enricher != null) {
            enricher.enrich(command);
        }
        OperationLogEntity entity = new OperationLogEntity();
        entity.setTenantId(command.getTenantId());
        entity.setStoreId(command.getStoreId());
        entity.setDomain(command.getDomain());
        entity.setOperatorId(command.getOperatorId());
        entity.setOperationType(command.getOperationType());
        entity.setTargetType(command.getTargetType());
        entity.setTargetId(command.getTargetId());
        entity.setDetail(command.getDetail());
        entity.setTraceId(command.getTraceId());
        entity.setUserIp(command.getUserIp());
        entity.setUserAgent(command.getUserAgent());
        entity.setRequestMethod(command.getRequestMethod());
        entity.setRequestUrl(command.getRequestUrl());
        operationLogMapper.insert(entity);
    }
}
