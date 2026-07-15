package com.tandiantong.security.audit;

import com.tandiantong.common.trace.TraceIdContext;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.OperationLogEntity;
import com.tandiantong.security.mapper.OperationLogMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** 数据库关键后台操作审计服务。 */
@Service
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseOperationAuditService implements OperationAuditService {

    private final OperationLogMapper operationLogMapper;

    public DatabaseOperationAuditService(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    /** 将关键操作写入独立审计日志表。 */
    @Override
    public void record(CurrentUser currentUser, String operationType, String targetType, String targetId, String detail) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setTenantId(currentUser.tenantId());
        entity.setStoreId(currentUser.storeId());
        entity.setDomain(currentUser.domain().name());
        entity.setOperatorId(currentUser.userId());
        entity.setOperationType(operationType);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setDetail(detail);
        entity.setTraceId(TraceIdContext.get());
        operationLogMapper.insert(entity);
    }
}
