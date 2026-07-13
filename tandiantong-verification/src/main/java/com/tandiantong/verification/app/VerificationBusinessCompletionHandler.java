package com.tandiantong.verification.app;

/**
 * 核销业务完成处理器，由具体业务模块实现其自身状态推进，避免核销模块跨模块访问 Mapper。
 */
public interface VerificationBusinessCompletionHandler {

    /**
     * 判断处理器是否支持指定业务类型。
     */
    boolean supports(String businessType);

    /**
     * 在核销凭证首次成功使用后推进对应业务状态。
     */
    void complete(Long tenantId, Long storeId, String businessNo);
}
