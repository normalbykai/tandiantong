package com.tandiantong.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.reservation.app.ReservationVerificationCompletionHandler;
import com.tandiantong.reservation.mapper.ServiceReservationMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * 预约核销完成处理器测试，覆盖合法履约和非法状态保护。
 */
class ReservationVerificationCompletionHandlerTest {

    @Test
    void shouldFulfillConfirmedReservationAfterVerification() {
        ServiceReservationMapper mapper = Mockito.mock(ServiceReservationMapper.class);
        when(mapper.fulfillAfterVerification(1L, 2L, "YY1001", "CONFIRMED", "FULFILLED"))
                .thenReturn(1);
        ReservationVerificationCompletionHandler handler = new ReservationVerificationCompletionHandler(mapper);

        handler.complete(1L, 2L, "YY1001");

        verify(mapper).fulfillAfterVerification(1L, 2L, "YY1001", "CONFIRMED", "FULFILLED");
    }

    @Test
    void shouldRejectReservationVerificationWhenStatusIsNotConfirmed() {
        ServiceReservationMapper mapper = Mockito.mock(ServiceReservationMapper.class);
        when(mapper.fulfillAfterVerification(1L, 2L, "YY1002", "CONFIRMED", "FULFILLED"))
                .thenReturn(0);
        ReservationVerificationCompletionHandler handler = new ReservationVerificationCompletionHandler(mapper);

        assertThatThrownBy(() -> handler.complete(1L, 2L, "YY1002"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("核销对应预约状态异常");
    }
}
