package com.tandiantong.framework.operatelog.core.service;

import com.tandiantong.framework.operatelog.core.model.OperationLogCommand;

/** 操作日志记录器，具体落库由业务模块实现。 */
public interface OperationLogRecorder {

    void record(OperationLogCommand command);
}
