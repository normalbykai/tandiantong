package com.tandiantong.reservation.app;

import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.reservation.mapper.ServiceReservationMapper;
import com.tandiantong.verification.app.VerificationBusinessCompletionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 服务预约核销完成处理器，负责以状态条件更新推进预约履约状态。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class ReservationVerificationCompletionHandler implements VerificationBusinessCompletionHandler {

    private static final String RESERVATION_TYPE = "RESERVATION";
    private static final String CONFIRMED_STATUS = "CONFIRMED";
    private static final String FULFILLED_STATUS = "FULFILLED";

    private final ServiceReservationMapper serviceReservationMapper;

    @Override
    public boolean supports(String businessType) {
        return RESERVATION_TYPE.equals(businessType);
    }

    @Override
    public void complete(Long tenantId, Long storeId, String businessNo) {
        int updated = serviceReservationMapper.fulfillAfterVerification(tenantId, storeId, businessNo,
                CONFIRMED_STATUS, FULFILLED_STATUS);
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "核销对应预约状态异常");
        }
    }
}
