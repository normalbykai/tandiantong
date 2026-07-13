package com.tandiantong.order.app;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.order.mapper.SalesOrderMapper;
import com.tandiantong.verification.app.VerificationBusinessCompletionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 商品订单核销完成处理器，负责以状态条件更新推进订单完成状态。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class OrderVerificationCompletionHandler implements VerificationBusinessCompletionHandler {

    private static final String PRODUCT_ORDER_TYPE = "PRODUCT_ORDER";
    private static final String PENDING_VERIFY_STATUS = "PENDING_VERIFY";
    private static final String COMPLETED_STATUS = "COMPLETED";

    private final SalesOrderMapper salesOrderMapper;

    @Override
    public boolean supports(String businessType) {
        return PRODUCT_ORDER_TYPE.equals(businessType);
    }

    @Override
    public void complete(Long tenantId, Long storeId, String businessNo) {
        int updated = salesOrderMapper.updateStatus(tenantId, storeId, businessNo,
                PENDING_VERIFY_STATUS, COMPLETED_STATUS);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "核销对应订单状态异常");
        }
    }
}
