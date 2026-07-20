package com.tandiantong.security.audit;

import com.tandiantong.framework.operatelog.core.model.OperationLogCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** 数据库能力关闭时使用的空审计服务，仅用于无数据库的应用测试。 */
@Service
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "false")
public class DisabledOperationAuditService implements OperationAuditService {

    /** 无数据库测试环境不写入审计记录。 */
    @Override
    public void record(OperationLogCommand command) {
    }
}
