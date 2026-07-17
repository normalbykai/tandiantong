package com.tandiantong.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.integration.wechatpay.LocalWechatPayClient;
import com.tandiantong.integration.wechatpay.WechatPayClient;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.reservation.entity.ServiceReservationEntity;
import com.tandiantong.reservation.mapper.ReservationIdempotencyRecordMapper;
import com.tandiantong.reservation.mapper.ServiceItemMapper;
import com.tandiantong.reservation.mapper.ServiceReservationMapper;
import com.tandiantong.reservation.mapper.ServiceSlotMapper;
import com.tandiantong.security.tenant.MerchantSceneService;
import com.tandiantong.verification.app.VerificationPersistenceService;
import java.time.LocalDateTime;
import java.util.List;
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
        WechatPayClient payClient = Mockito.mock(WechatPayClient.class);
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        when(sceneService.resolveEnabledScene("scene-test"))
                .thenReturn(new MerchantSceneService.MerchantSceneScope(1L, 2L));
        when(idempotencyMapper.selectOne(any())).thenReturn(null);
        var serviceItem = new com.tandiantong.reservation.entity.ServiceItemEntity();
        serviceItem.setId(3L);
        serviceItem.setTenantId(1L);
        serviceItem.setStoreId(2L);
        serviceItem.setName("咖啡体验课");
        serviceItem.setPaymentMode("FREE");
        serviceItem.setPriceCent(0);
        when(itemMapper.selectReservableService(1L, 2L, 3L, 4L, "ENABLED")).thenReturn(serviceItem);
        when(slotMapper.occupyCapacity(1L, 2L, 4L)).thenReturn(0);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);

        assertThatThrownBy(() -> service.reserve("scene-test",
                new ReservationPersistenceService.ReserveCommand("idem-1", 3L, 4L, "13800000000")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预约时段剩余名额不足");
        verify(reservationMapper, never()).insert(any(ServiceReservationEntity.class));
    }

    @Test
    void shouldConfirmPaidReservationCallbackAndIssueCredential() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        LocalWechatPayClient payClient = new LocalWechatPayClient("local-secret");
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        ServiceReservationEntity reservation = new ServiceReservationEntity();
        reservation.setTenantId(1L);
        reservation.setStoreId(2L);
        reservation.setReservationNo("YY1PAID001");
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setPayAmountCent(9900);
        reservation.setPrepayId("LOCAL-PREPAY-YY1PAID001");
        when(reservationMapper.selectByReservationNo("YY1PAID001")).thenReturn(reservation);
        when(reservationMapper.confirmPayment(1L, 2L, "YY1PAID001", "PENDING_PAYMENT", "CONFIRMED", "TX-001"))
                .thenReturn(1);
        when(verificationService.issueReservationCredential(1L, 2L, "YY1PAID001", "服务预约 YY1PAID001"))
                .thenReturn(new VerificationPersistenceService.Credential("YY1PAID001", "A001", "vk-token", "PENDING"));
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);
        String signature = payClient.signCallback("YY1PAID001", "TX-001", 9900);

        var result = service.confirmPayment("YY1PAID001", "TX-001", 9900, signature);

        org.assertj.core.api.Assertions.assertThat(result.status()).isEqualTo("CONFIRMED");
        org.assertj.core.api.Assertions.assertThat(result.pickupNo()).isEqualTo("A001");
        org.assertj.core.api.Assertions.assertThat(result.verificationToken()).isEqualTo("vk-token");
        verify(reservationMapper).attachVoucher(1L, 2L, "YY1PAID001", "A001");
    }

    @Test
    void shouldRejectPaidReservationCallbackWithWrongAmount() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        LocalWechatPayClient payClient = new LocalWechatPayClient("local-secret");
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        ServiceReservationEntity reservation = new ServiceReservationEntity();
        reservation.setTenantId(1L);
        reservation.setStoreId(2L);
        reservation.setReservationNo("YY1PAID002");
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setPayAmountCent(9900);
        when(reservationMapper.selectByReservationNo("YY1PAID002")).thenReturn(reservation);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);
        String signature = payClient.signCallback("YY1PAID002", "TX-002", 9800);

        assertThatThrownBy(() -> service.confirmPayment("YY1PAID002", "TX-002", 9800, signature))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预约支付状态或金额不正确");
        verify(verificationService, never()).issueReservationCredential(any(), any(), any(), any());
    }

    @Test
    void shouldReturnConfirmedReservationForRepeatedPaymentCallbackWithoutSideEffects() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        LocalWechatPayClient payClient = new LocalWechatPayClient("local-secret");
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        ServiceReservationEntity reservation = customerReservation("YY1PAID003", "CONFIRMED", "13800000000");
        reservation.setPayAmountCent(9900);
        when(reservationMapper.selectByReservationNo("YY1PAID003")).thenReturn(reservation);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);
        String signature = payClient.signCallback("YY1PAID003", "TX-003", 9900);

        var result = service.confirmPayment("YY1PAID003", "TX-003", 9900, signature);

        org.assertj.core.api.Assertions.assertThat(result.status()).isEqualTo("CONFIRMED");
        verify(reservationMapper, never()).confirmPayment(any(), any(), any(), any(), any(), any());
        verify(verificationService, never()).issueReservationCredential(any(), any(), any(), any());
    }

    @Test
    void shouldRejectPaymentCallbackWhenReservationAlreadyCanceled() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        LocalWechatPayClient payClient = new LocalWechatPayClient("local-secret");
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        ServiceReservationEntity reservation = customerReservation("YY1PAID004", "CANCELED", "13800000000");
        reservation.setPayAmountCent(9900);
        when(reservationMapper.selectByReservationNo("YY1PAID004")).thenReturn(reservation);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);
        String signature = payClient.signCallback("YY1PAID004", "TX-004", 9900);

        assertThatThrownBy(() -> service.confirmPayment("YY1PAID004", "TX-004", 9900, signature))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预约支付状态或金额不正确");
        verify(reservationMapper, never()).confirmPayment(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldCancelExpiredPendingReservationAndReleaseCapacityOnce() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        WechatPayClient payClient = Mockito.mock(WechatPayClient.class);
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        LocalDateTime expireBefore = LocalDateTime.of(2026, 7, 17, 10, 0);
        ServiceReservationEntity expired = new ServiceReservationEntity();
        expired.setTenantId(1L);
        expired.setStoreId(2L);
        expired.setSlotId(4L);
        expired.setReservationNo("YY1EXPIRED001");
        expired.setStatus("PENDING_PAYMENT");
        expired.setExpireAt(expireBefore.minusMinutes(1));
        when(reservationMapper.selectExpiredPendingReservations(1L, 2L, "PENDING_PAYMENT", expireBefore))
                .thenReturn(List.of(expired));
        when(reservationMapper.cancelPendingPayment(1L, 2L, "YY1EXPIRED001", "PENDING_PAYMENT", "CANCELED"))
                .thenReturn(1);
        when(slotMapper.releaseCapacity(1L, 2L, 4L)).thenReturn(1);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);

        int count = service.cancelExpiredPendingReservations(1L, 2L, expireBefore);

        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(1);
        verify(slotMapper).releaseCapacity(1L, 2L, 4L);
    }

    @Test
    void shouldNotReleaseCapacityWhenExpiredReservationAlreadyCanceledConcurrently() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        WechatPayClient payClient = Mockito.mock(WechatPayClient.class);
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        LocalDateTime expireBefore = LocalDateTime.of(2026, 7, 17, 10, 0);
        ServiceReservationEntity expired = new ServiceReservationEntity();
        expired.setTenantId(1L);
        expired.setStoreId(2L);
        expired.setSlotId(4L);
        expired.setReservationNo("YY1EXPIRED002");
        expired.setStatus("PENDING_PAYMENT");
        expired.setExpireAt(expireBefore.minusMinutes(1));
        when(reservationMapper.selectExpiredPendingReservations(1L, 2L, "PENDING_PAYMENT", expireBefore))
                .thenReturn(List.of(expired));
        when(reservationMapper.cancelPendingPayment(1L, 2L, "YY1EXPIRED002", "PENDING_PAYMENT", "CANCELED"))
                .thenReturn(0);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);

        int count = service.cancelExpiredPendingReservations(1L, 2L, expireBefore);

        org.assertj.core.api.Assertions.assertThat(count).isZero();
        verify(slotMapper, never()).releaseCapacity(any(), any(), any());
    }

    @Test
    void shouldListCustomerReservationsWithinTenantStoreAndContactMobile() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        WechatPayClient payClient = Mockito.mock(WechatPayClient.class);
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        ServiceReservationEntity reservation = customerReservation("YY1CUSTOMER001", "CONFIRMED", "13800000000");
        when(reservationMapper.selectList(any())).thenReturn(List.of(reservation));
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);

        var result = service.listCustomerReservations(1L, 2L, "13800000000", "CONFIRMED");

        org.assertj.core.api.Assertions.assertThat(result).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(result.getFirst().getReservationNo()).isEqualTo("YY1CUSTOMER001");
        org.assertj.core.api.Assertions.assertThat(result.getFirst().getPickupNo()).isEqualTo("A001");
    }

    @Test
    void shouldRejectCustomerReservationDetailWhenContactMobileDoesNotMatch() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        WechatPayClient payClient = Mockito.mock(WechatPayClient.class);
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        when(reservationMapper.selectOne(any())).thenReturn(null);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);

        assertThatThrownBy(() -> service.getCustomerReservationDetail(1L, 2L, "YY1CUSTOMER002", "13800000000"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预约不存在");
    }

    @Test
    void shouldRejectCustomerCancelWhenContactMobileDoesNotMatch() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        WechatPayClient payClient = Mockito.mock(WechatPayClient.class);
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        when(reservationMapper.selectOne(any())).thenReturn(null);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);

        assertThatThrownBy(() -> service.cancelCustomerReservation(1L, 2L, "YY1CUSTOMER004", "13800000000"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("预约不存在");
        verify(reservationMapper, never()).selectForUpdate(any(), any(), any());
        verify(slotMapper, never()).releaseCapacity(any(), any(), any());
    }

    @Test
    void shouldCancelCustomerReservationAfterContactBoundaryCheck() {
        ServiceItemMapper itemMapper = Mockito.mock(ServiceItemMapper.class);
        ServiceSlotMapper slotMapper = Mockito.mock(ServiceSlotMapper.class);
        ServiceReservationMapper reservationMapper = Mockito.mock(ServiceReservationMapper.class);
        ReservationIdempotencyRecordMapper idempotencyMapper = Mockito.mock(ReservationIdempotencyRecordMapper.class);
        MerchantSceneService sceneService = Mockito.mock(MerchantSceneService.class);
        WechatPayClient payClient = Mockito.mock(WechatPayClient.class);
        VerificationPersistenceService verificationService = Mockito.mock(VerificationPersistenceService.class);
        ServiceReservationEntity reservation = customerReservation("YY1CUSTOMER003", "CONFIRMED", "13800000000");
        when(reservationMapper.selectOne(any())).thenReturn(reservation);
        when(reservationMapper.selectForUpdate(1L, 2L, "YY1CUSTOMER003")).thenReturn(reservation);
        when(reservationMapper.cancel(1L, 2L, "YY1CUSTOMER003", "CANCELED", "CONFIRMED", "PENDING_PAYMENT"))
                .thenReturn(1);
        when(slotMapper.releaseCapacity(1L, 2L, 4L)).thenReturn(1);
        ReservationPersistenceService service = new ReservationPersistenceService(
                itemMapper, slotMapper, reservationMapper, idempotencyMapper, sceneService, payClient, verificationService);

        var result = service.cancelCustomerReservation(1L, 2L, "YY1CUSTOMER003", "13800000000");

        org.assertj.core.api.Assertions.assertThat(result.status()).isEqualTo("CANCELED");
        verify(slotMapper).releaseCapacity(1L, 2L, 4L);
    }

    private ServiceReservationEntity customerReservation(String reservationNo, String status, String contactMobile) {
        ServiceReservationEntity reservation = new ServiceReservationEntity();
        reservation.setTenantId(1L);
        reservation.setStoreId(2L);
        reservation.setServiceId(3L);
        reservation.setSlotId(4L);
        reservation.setReservationNo(reservationNo);
        reservation.setStatus(status);
        reservation.setContactMobile(contactMobile);
        reservation.setPayAmountCent(9900);
        reservation.setVoucherCode("A001");
        reservation.setPrepayId("LOCAL-PREPAY-" + reservationNo);
        reservation.setCreatedAt(LocalDateTime.of(2026, 7, 17, 10, 0));
        return reservation;
    }
}
