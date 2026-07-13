package com.tandiantong.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.reservation.entity.ServiceReservationEntity;
import com.tandiantong.reservation.mapper.ReservationIdempotencyRecordMapper;
import com.tandiantong.reservation.mapper.ServiceItemMapper;
import com.tandiantong.reservation.mapper.ServiceReservationMapper;
import com.tandiantong.reservation.mapper.ServiceSlotMapper;
import com.tandiantong.security.tenant.MerchantSceneService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * 预约持久化服务测试，覆盖并发容量条件更新失败时的业务保护。
 */
class ReservationPersistenceServiceTest {

    @Test
    void shouldRejectReservationWhenAtomicCapacityOccupationFails() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        when(sceneService.resolveEnabledScene("scene-test"))
                .thenReturn(new MerchantSceneService.MerchantSceneScope(1L, 2L));
        when(idempotencyMapper.selectOne(any())).thenReturn(null);
        when(itemMapper.selectPaymentMode(1L, 2L, 3L, 4L, "ENABLED")).thenReturn("FREE");
        when(slotMapper.occupyCapacity(1L, 2L, 4L)).thenReturn(0);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService);

        assertThatThrownBy(() -> service.reserve("scene-test",
                new ReservationPersistenceService.ReserveCommand("idem-1", 3L, 4L, "13800000000")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预约时段剩余名额不足");
        verify(reservationMapper, never()).insert(any(ServiceReservationEntity.class));
    }
}
