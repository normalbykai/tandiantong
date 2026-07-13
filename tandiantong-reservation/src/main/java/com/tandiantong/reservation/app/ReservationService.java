package com.tandiantong.reservation.app;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.reservation.domain.PaymentMode;
import com.tandiantong.reservation.domain.ReservationRecord;
import com.tandiantong.reservation.domain.ReservationStatus;
import com.tandiantong.reservation.domain.ServiceItem;
import com.tandiantong.reservation.domain.ServiceSlot;
import com.tandiantong.reservation.tenant.TenantStoreScope;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** 服务预约、容量占用和取消领域服务。 */
public class ReservationService {

    private final AtomicLong idSequence = new AtomicLong(1000);
    private final Map<Long, ServiceItem> services = new LinkedHashMap<>();
    private final Map<Long, ServiceSlot> slots = new LinkedHashMap<>();
    private final Map<String, ReservationRecord> reservationsByNo = new LinkedHashMap<>();
    private final Map<String, ReservationRecord> reservationsByIdempotencyKey = new LinkedHashMap<>();

    public ServiceItem createServiceItem(TenantStoreScope scope, String serviceName,
                                         PaymentMode paymentMode, int priceCent, int durationMinutes) {
        if (priceCent < 0 || durationMinutes <= 0) {
            throw businessError("服务价格和时长不合法");
        }
        ServiceItem serviceItem = new ServiceItem(idSequence.incrementAndGet(), scope.tenantId(), scope.storeId(),
                serviceName, paymentMode, priceCent, durationMinutes);
        services.put(serviceItem.serviceId(), serviceItem);
        return serviceItem;
    }

    public ServiceSlot publishSlot(TenantStoreScope scope, Long serviceId, LocalDate serviceDate,
                                   String startTime, String endTime, int capacity) {
        ServiceItem serviceItem = services.get(serviceId);
        ensureServiceBelongsToScope(scope, serviceItem);
        if (capacity <= 0) {
            throw businessError("预约时段容量必须大于零");
        }
        ServiceSlot slot = new ServiceSlot(idSequence.incrementAndGet(), scope.tenantId(), scope.storeId(),
                serviceId, serviceDate, startTime, endTime, capacity, 0, false, 0);
        slots.put(slot.slotId(), slot);
        return slot;
    }

    public ReservationRecord createReservation(TenantStoreScope scope, CreateReservationCommand command) {
        String reservationKey = scope.tenantId() + ":" + command.idempotencyKey();
        if (reservationsByIdempotencyKey.containsKey(reservationKey)) {
            return reservationsByIdempotencyKey.get(reservationKey);
        }
        ServiceItem serviceItem = services.get(command.serviceId());
        ServiceSlot slot = slots.get(command.slotId());
        ensureServiceBelongsToScope(scope, serviceItem);
        ensureSlotBelongsToScope(scope, slot);
        if (!slot.serviceId().equals(serviceItem.serviceId())) {
            throw businessError("预约时段不属于当前服务项目");
        }
        ServiceSlot occupiedSlot = occupySlot(slot);
        slots.put(slot.slotId(), occupiedSlot);
        String reservationNo = "YY" + scope.tenantId() + idSequence.incrementAndGet();
        ReservationStatus status = serviceItem.paymentMode() == PaymentMode.FREE
                ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING_PAYMENT;
        String voucherCode = status == ReservationStatus.CONFIRMED ? voucherCode(reservationNo) : null;
        ReservationRecord reservation = new ReservationRecord(idSequence.incrementAndGet(), scope.tenantId(),
                scope.storeId(), serviceItem.serviceId(), slot.slotId(), reservationNo, status,
                command.contactMobile(), voucherCode, null, Instant.now());
        reservationsByNo.put(reservationNo, reservation);
        reservationsByIdempotencyKey.put(reservationKey, reservation);
        return reservation;
    }

    public ReservationRecord confirmPaidReservation(TenantStoreScope scope, String reservationNo, String transactionId) {
        ReservationRecord reservation = findReservation(scope, reservationNo);
        if (reservation.status() == ReservationStatus.CONFIRMED) {
            return reservation;
        }
        if (reservation.status() != ReservationStatus.PENDING_PAYMENT) {
            throw businessError("当前预约状态不能确认支付");
        }
        ReservationRecord confirmed = reservation.confirmed(voucherCode(reservationNo), transactionId);
        reservationsByNo.put(reservationNo, confirmed);
        return confirmed;
    }

    public void cancelReservation(TenantStoreScope scope, String reservationNo, String reason) {
        ReservationRecord reservation = findReservation(scope, reservationNo);
        if (reservation.status() == ReservationStatus.FULFILLED) {
            throw businessError("已履约预约不能取消");
        }
        if (reservation.status() == ReservationStatus.CANCELED) {
            return;
        }
        ServiceSlot slot = findSlot(scope, reservation.slotId());
        slots.put(slot.slotId(), slot.release());
        reservationsByNo.put(reservationNo, reservation.withStatus(ReservationStatus.CANCELED));
    }

    public ServiceSlot findSlot(TenantStoreScope scope, Long slotId) {
        ServiceSlot slot = slots.get(slotId);
        ensureSlotBelongsToScope(scope, slot);
        return slot;
    }

    public ReservationRecord findReservation(TenantStoreScope scope, String reservationNo) {
        ReservationRecord reservation = reservationsByNo.get(reservationNo);
        if (reservation == null || !reservation.tenantId().equals(scope.tenantId())
                || !reservation.storeId().equals(scope.storeId())) {
            throw businessError("预约资源不属于当前租户或门店");
        }
        return reservation;
    }

    private ServiceSlot occupySlot(ServiceSlot slot) {
        if (slot.paused()) {
            throw businessError("预约时段已暂停");
        }
        if (slot.usedCapacity() >= slot.capacity()) {
            throw businessError("预约时段剩余名额不足");
        }
        return slot.occupy();
    }

    private void ensureServiceBelongsToScope(TenantStoreScope scope, ServiceItem serviceItem) {
        if (serviceItem == null || !serviceItem.tenantId().equals(scope.tenantId())
                || !serviceItem.storeId().equals(scope.storeId())) {
            throw businessError("预约资源不属于当前租户或门店");
        }
    }

    private void ensureSlotBelongsToScope(TenantStoreScope scope, ServiceSlot slot) {
        if (slot == null || !slot.tenantId().equals(scope.tenantId()) || !slot.storeId().equals(scope.storeId())) {
            throw businessError("预约资源不属于当前租户或门店");
        }
    }

    private String voucherCode(String reservationNo) {
        return "RV-" + reservationNo;
    }

    private BusinessException businessError(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
