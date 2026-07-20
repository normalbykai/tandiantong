package com.tandiantong.framework.common.context;

/** 线程上下文清理器，用于请求结束后释放 ThreadLocal 状态。 */
public interface ThreadLocalContextCleaner {

    void clear();
}
