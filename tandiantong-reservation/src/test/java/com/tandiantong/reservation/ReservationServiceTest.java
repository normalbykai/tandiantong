package com.tandiantong.reservation;

import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.reservation.app.CreateReservationCommand;
import com.tandiantong.reservation.app.ReservationService;
import com.tandiantong.reservation.domain.PaymentMode;
import com.tandiantong.reservation.domain.ReservationStatus;
import com.tandiantong.reservation.tenant.TenantStoreScope;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationServiceTest {

    private final TenantStoreScope scope = new TenantStoreScope(1001L, 2001L, 3001L);

    @Test
    void shouldConfirmFreeReservationAndGenerateVoucher() {
        ReservationService service = new ReservationService();
        var serviceItem = service.createServiceItem(scope, "咖啡体验课", PaymentMode.FREE, 0, 60);
        var slot = service.publishSlot(scope, serviceItem.serviceId(), LocalDate.of(2026, 7, 13),
                "14:00", "15:00", 2);

        var reservation = service.createReservation(scope,
                new CreateReservationCommand("FREE-001", serviceItem.serviceId(), slot.slotId(), "13800008000"));

        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.voucherCode()).startsWith("RV");
        assertThat(service.findSlot(scope, slot.slotId()).usedCapacity()).isEqualTo(1);
    }

    @Test
    void shouldRejectOverCapacityReservation() {
        ReservationService service = new ReservationService();
        var serviceItem = service.createServiceItem(scope, "咖啡体验课", PaymentMode.FREE, 0, 60);
        var slot = service.publishSlot(scope, serviceItem.serviceId(), LocalDate.of(2026, 7, 13),
                "15:00", "16:00", 1);

        service.createReservation(scope,
                new CreateReservationCommand("FREE-002", serviceItem.serviceId(), slot.slotId(), "13800008000"));

        assertThatThrownBy(() -> service.createReservation(scope,
                new CreateReservationCommand("FREE-003", serviceItem.serviceId(), slot.slotId(), "13900006123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("预约时段剩余名额不足");
    }

    @Test
    void shouldCreatePaidReservationPendingPaymentThenConfirmAfterPayment() {
        ReservationService service = new ReservationService();
        var serviceItem = service.createServiceItem(scope, "咖啡体验课", PaymentMode.WECHAT_PAY, 9900, 60);
        var slot = service.publishSlot(scope, serviceItem.serviceId(), LocalDate.of(2026, 7, 14),
                "10:00", "11:00", 2);

        var reservation = service.createReservation(scope,
                new CreateReservationCommand("PAY-001", serviceItem.serviceId(), slot.slotId(), "13800008000"));
        var confirmed = service.confirmPaidReservation(scope, reservation.reservationNo(), "TX-RES-001");

        assertThat(reservation.status()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(reservation.voucherCode()).isNull();
        assertThat(confirmed.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(confirmed.voucherCode()).startsWith("RV");
    }

    @Test
    void shouldReleaseCapacityWhenCancelReservation() {
        ReservationService service = new ReservationService();
        var serviceItem = service.createServiceItem(scope, "咖啡体验课", PaymentMode.FREE, 0, 60);
        var slot = service.publishSlot(scope, serviceItem.serviceId(), LocalDate.of(2026, 7, 14),
                "11:00", "12:00", 1);
        var reservation = service.createReservation(scope,
                new CreateReservationCommand("FREE-004", serviceItem.serviceId(), slot.slotId(), "13800008000"));

        service.cancelReservation(scope, reservation.reservationNo(), "顾客临时有事");

        assertThat(service.findReservation(scope, reservation.reservationNo()).status()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(service.findSlot(scope, slot.slotId()).usedCapacity()).isZero();
    }

    @Test
    void shouldReturnSameResultForRepeatedReservationRequest() {
        ReservationService service = new ReservationService();
        var serviceItem = service.createServiceItem(scope, "咖啡体验课", PaymentMode.FREE, 0, 60);
        var slot = service.publishSlot(scope, serviceItem.serviceId(), LocalDate.of(2026, 7, 15),
                "14:00", "15:00", 2);

        var first = service.createReservation(scope,
                new CreateReservationCommand("FREE-005", serviceItem.serviceId(), slot.slotId(), "13800008000"));
        var repeated = service.createReservation(scope,
                new CreateReservationCommand("FREE-005", serviceItem.serviceId(), slot.slotId(), "13800008000"));

        assertThat(repeated.reservationNo()).isEqualTo(first.reservationNo());
        assertThat(service.findSlot(scope, slot.slotId()).usedCapacity()).isEqualTo(1);
    }

    @Test
    void shouldRejectCrossTenantReservationMutation() {
        ReservationService service = new ReservationService();
        var serviceItem = service.createServiceItem(scope, "咖啡体验课", PaymentMode.FREE, 0, 60);
        var slot = service.publishSlot(scope, serviceItem.serviceId(), LocalDate.of(2026, 7, 15),
                "16:00", "17:00", 2);
        TenantStoreScope otherTenant = new TenantStoreScope(1002L, 2002L, 3002L);

        assertThatThrownBy(() -> service.createReservation(otherTenant,
                new CreateReservationCommand("FREE-006", serviceItem.serviceId(), slot.slotId(), "13900006123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("预约资源不属于当前租户或门店");
    }
}
