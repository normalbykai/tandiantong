package com.tandiantong.security.audit;

import com.tandiantong.security.context.CurrentUser;

/** 关键后台操作审计服务。 */
public interface OperationAuditService {

    /** 记录已完成的关键操作，审计内容不包含敏感凭证和支付报文。 */
    void record(CurrentUser currentUser, String operationType, String targetType, String targetId, String detail);
}
