package com.tandiantong.security.audit;

import com.tandiantong.framework.common.trace.TraceIdContext;
import com.tandiantong.framework.operatelog.core.model.OperationLogCommand;
import com.tandiantong.framework.operatelog.core.service.OperationLogRecorder;
import com.tandiantong.security.context.CurrentUser;

/** 关键后台操作审计服务。 */
public interface OperationAuditService extends OperationLogRecorder {

    /** 记录已完成的关键操作，审计内容不包含敏感凭证和支付报文。 */
    default void record(CurrentUser currentUser, String operationType, String targetType, String targetId, String detail) {
        OperationLogCommand command = new OperationLogCommand();
        command.setTenantId(currentUser.tenantId());
        command.setStoreId(currentUser.storeId());
        command.setDomain(currentUser.domain().name());
        command.setOperatorId(currentUser.userId());
        command.setOperationType(operationType);
        command.setTargetType(targetType);
        command.setTargetId(targetId);
        command.setDetail(detail);
        command.setTraceId(TraceIdContext.get());
        record(command);
    }
}
