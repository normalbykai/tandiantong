package com.tandiantong.reservation.app;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.reservation.entity.ReservationIdempotencyRecordEntity;
import com.tandiantong.reservation.entity.ServiceItemEntity;
import com.tandiantong.reservation.entity.ServiceReservationEntity;
import com.tandiantong.reservation.entity.ServiceSlotEntity;
import com.tandiantong.reservation.mapper.ReservationIdempotencyRecordMapper;
import com.tandiantong.reservation.mapper.ServiceItemMapper;
import com.tandiantong.reservation.mapper.ServiceReservationMapper;
import com.tandiantong.reservation.mapper.ServiceSlotMapper;
import com.tandiantong.reservation.tenant.TenantStoreScope;
import com.tandiantong.security.tenant.MerchantSceneService;
import com.tandiantong.security.tenant.MerchantSceneService.MerchantSceneScope;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预约应用服务，负责服务项目、可预约时段、容量占用、预约幂等和取消流程。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class ReservationPersistenceService {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final ServiceItemMapper serviceItemMapper;
    private final ServiceSlotMapper serviceSlotMapper;
    private final ServiceReservationMapper serviceReservationMapper;
    private final ReservationIdempotencyRecordMapper idempotencyRecordMapper;
    private final MerchantSceneService merchantSceneService;

    /**
     * 在可信租户门店范围内创建服务项目。
     */
    @Transactional
    public ServiceResult createService(TenantStoreScope scope, CreateServiceCommand command) {
        validateService(command);
        ServiceItemEntity service = new ServiceItemEntity();
        service.setTenantId(scope.tenantId());
        service.setStoreId(scope.storeId());
        service.setName(command.name());
        service.setPaymentMode(command.paymentMode());
        service.setPriceCent(command.priceCent());
        service.setDurationMinutes(command.durationMinutes());
        service.setStatus(ServiceStatus.ENABLED.code());
        serviceItemMapper.insert(service);
        return new ServiceResult(service.getId(), command.name(), command.paymentMode(),
                command.priceCent(), command.durationMinutes());
    }

    /**
     * 为启用服务创建预约时段。
     */
    @Transactional
    public SlotResult createSlot(TenantStoreScope scope, CreateSlotCommand command) {
        validateSlot(command);
        Long serviceCount = serviceItemMapper.selectCount(Wrappers.<ServiceItemEntity>lambdaQuery()
                .eq(ServiceItemEntity::getId, command.serviceId())
                .eq(ServiceItemEntity::getTenantId, scope.tenantId())
                .eq(ServiceItemEntity::getStoreId, scope.storeId())
                .eq(ServiceItemEntity::getStatus, ServiceStatus.ENABLED.code()));
        if (serviceCount != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "服务项目或时段容量不合法");
        }
        ServiceSlotEntity slot = new ServiceSlotEntity();
        slot.setTenantId(scope.tenantId());
        slot.setStoreId(scope.storeId());
        slot.setServiceId(command.serviceId());
        slot.setServiceDate(command.serviceDate());
        slot.setStartTime(command.startTime());
        slot.setEndTime(command.endTime());
        slot.setCapacity(command.capacity());
        slot.setUsedCapacity(0);
        slot.setPaused(false);
        slot.setVersion(0L);
        serviceSlotMapper.insert(slot);
        return new SlotResult(slot.getId(), command.serviceId(), command.serviceDate(),
                command.startTime(), command.endTime(), command.capacity(), 0);
    }

    /**
     * 按小程序入口码查询当前及未来可预约服务时段。
     */
    public List<MiniService> listByScene(String sceneKey) {
        MerchantSceneScope scope = merchantSceneService.resolveEnabledScene(sceneKey);
        return serviceSlotMapper.selectAvailableServices(scope.tenantId(), scope.storeId(),
                        ServiceStatus.ENABLED.code(), LocalDate.now(BUSINESS_ZONE_ID))
                .stream()
                .map(this::toMiniService)
                .toList();
    }

    /**
     * 原子占用预约容量并创建预约，相同幂等键返回首次预约结果。
     */
    @Transactional
    public ReservationResult reserve(String sceneKey, ReserveCommand command) {
        validateReserve(command);
        MerchantSceneScope scope = merchantSceneService.resolveEnabledScene(sceneKey);
        ReservationIdempotencyRecordEntity previous = idempotencyRecordMapper.selectOne(
                Wrappers.<ReservationIdempotencyRecordEntity>lambdaQuery()
                        .eq(ReservationIdempotencyRecordEntity::getTenantId, scope.tenantId())
                        .eq(ReservationIdempotencyRecordEntity::getBusinessType,
                                IdempotencyBusinessType.RESERVATION_CREATE.code())
                        .eq(ReservationIdempotencyRecordEntity::getIdempotencyKey, command.idempotencyKey()));
        if (previous != null) {
            ServiceReservationEntity reservation = serviceReservationMapper.selectOne(
                    Wrappers.<ServiceReservationEntity>lambdaQuery()
                            .eq(ServiceReservationEntity::getTenantId, scope.tenantId())
                            .eq(ServiceReservationEntity::getReservationNo, previous.getBusinessNo()));
            if (reservation != null) {
                return toResult(reservation);
            }
        }

        String paymentMode = serviceItemMapper.selectPaymentMode(scope.tenantId(), scope.storeId(),
                command.serviceId(), command.slotId(), ServiceStatus.ENABLED.code());
        if (paymentMode == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "预约服务或时段不存在");
        }
        if (serviceSlotMapper.occupyCapacity(scope.tenantId(), scope.storeId(), command.slotId()) != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约时段剩余名额不足");
        }

        String reservationNo = "YY" + scope.tenantId() + randomPart(14);
        ReservationStatus status = PaymentMode.FREE.matches(paymentMode)
                ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING_PAYMENT;
        String voucher = status == ReservationStatus.CONFIRMED ? "rv-" + randomPart(32) : null;
        ServiceReservationEntity reservation = new ServiceReservationEntity();
        reservation.setTenantId(scope.tenantId());
        reservation.setStoreId(scope.storeId());
        reservation.setServiceId(command.serviceId());
        reservation.setSlotId(command.slotId());
        reservation.setReservationNo(reservationNo);
        reservation.setStatus(status.code());
        reservation.setContactMobile(command.contactMobile());
        reservation.setVoucherCode(voucher);
        serviceReservationMapper.insert(reservation);
        insertIdempotency(scope.tenantId(), command.idempotencyKey(), reservationNo);
        return toResult(reservation);
    }

    /**
     * 取消未履约预约并释放容量，重复取消返回已取消结果。
     */
    @Transactional
    public ReservationResult cancel(TenantStoreScope scope, String reservationNo) {
        ServiceReservationEntity reservation = serviceReservationMapper.selectForUpdate(
                scope.tenantId(), scope.storeId(), reservationNo);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "预约不存在");
        }
        if (ReservationStatus.CANCELED.matches(reservation.getStatus())) {
            return toResult(reservation);
        }
        if (ReservationStatus.FULFILLED.matches(reservation.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "已履约预约不能取消");
        }
        int updated = serviceReservationMapper.cancel(scope.tenantId(), scope.storeId(), reservationNo,
                ReservationStatus.CANCELED.code(), ReservationStatus.CONFIRMED.code(),
                ReservationStatus.PENDING_PAYMENT.code());
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "当前预约状态不能取消");
        }
        if (serviceSlotMapper.releaseCapacity(scope.tenantId(), scope.storeId(), reservation.getSlotId()) != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约容量状态异常");
        }
        return new ReservationResult(reservationNo, ReservationStatus.CANCELED.code(),
                reservation.getVoucherCode());
    }

    private MiniService toMiniService(Map<String, Object> row) {
        int capacity = number(row, "capacity").intValue();
        int usedCapacity = number(row, "used_capacity").intValue();
        return new MiniService(number(row, "service_id").longValue(), string(row, "service_name"),
                string(row, "payment_mode"), number(row, "price_cent").intValue(),
                number(row, "duration_minutes").intValue(), number(row, "slot_id").longValue(),
                (LocalDate) value(row, "service_date"), string(row, "start_time"),
                string(row, "end_time"), capacity - usedCapacity);
    }

    private Number number(Map<String, Object> row, String key) {
        return (Number) value(row, key);
    }

    private String string(Map<String, Object> row, String key) {
        Object value = value(row, key);
        return value == null ? null : value.toString();
    }

    private Object value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? row.get(key.toUpperCase()) : value;
    }

    private void insertIdempotency(Long tenantId, String key, String reservationNo) {
        ReservationIdempotencyRecordEntity record = new ReservationIdempotencyRecordEntity();
        record.setTenantId(tenantId);
        record.setIdempotencyKey(key);
        record.setBusinessType(IdempotencyBusinessType.RESERVATION_CREATE.code());
        record.setBusinessNo(reservationNo);
        record.setResultStatus(IdempotencyResult.SUCCESS.code());
        idempotencyRecordMapper.insert(record);
    }

    private ReservationResult toResult(ServiceReservationEntity reservation) {
        return new ReservationResult(reservation.getReservationNo(), reservation.getStatus(),
                reservation.getVoucherCode());
    }

    private void validateService(CreateServiceCommand command) {
        if (command == null || command.name() == null || command.name().isBlank()
                || !PaymentMode.supports(command.paymentMode())
                || command.priceCent() < 0 || command.durationMinutes() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "服务项目参数不合法");
        }
    }

    private void validateSlot(CreateSlotCommand command) {
        if (command == null || command.serviceId() == null || command.serviceDate() == null
                || command.startTime() == null || command.startTime().isBlank()
                || command.endTime() == null || command.endTime().isBlank()
                || command.capacity() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "服务项目或时段容量不合法");
        }
    }

    private void validateReserve(ReserveCommand command) {
        if (command == null || command.idempotencyKey() == null || command.idempotencyKey().isBlank()
                || command.serviceId() == null || command.slotId() == null
                || command.contactMobile() == null || !command.contactMobile().matches("1\\d{10}")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约参数不合法");
        }
    }

    private String randomPart(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    /** 创建服务项目命令。 */
    public record CreateServiceCommand(String name, String paymentMode, int priceCent, int durationMinutes) {
    }

    /** 创建预约时段命令。 */
    public record CreateSlotCommand(Long serviceId, LocalDate serviceDate,
                                    String startTime, String endTime, int capacity) {
    }

    /** 顾客预约命令。 */
    public record ReserveCommand(String idempotencyKey, Long serviceId, Long slotId, String contactMobile) {
    }

    /** 服务项目处理结果。 */
    public record ServiceResult(Long serviceId, String name, String paymentMode,
                                int priceCent, int durationMinutes) {
    }

    /** 预约时段处理结果。 */
    public record SlotResult(Long slotId, Long serviceId, LocalDate serviceDate,
                             String startTime, String endTime, int capacity, int usedCapacity) {
    }

    /** 小程序可预约服务展示数据。 */
    public record MiniService(Long serviceId, String name, String paymentMode,
                              int priceCent, int durationMinutes, Long slotId,
                              LocalDate serviceDate, String startTime, String endTime,
                              int remainingCapacity) {
    }

    /** 预约处理结果。 */
    public record ReservationResult(String reservationNo, String status, String voucherCode) {
    }

    /** 服务启停状态。 */
    private enum ServiceStatus {
        ENABLED;

        String code() {
            return name();
        }
    }

    /** 服务支付模式。 */
    private enum PaymentMode {
        FREE,
        PAID;

        static boolean supports(String value) {
            return FREE.matches(value) || PAID.matches(value);
        }

        boolean matches(String value) {
            return name().equals(value);
        }
    }

    /** 预约状态。 */
    private enum ReservationStatus {
        PENDING_PAYMENT,
        CONFIRMED,
        CANCELED,
        FULFILLED;

        String code() {
            return name();
        }

        boolean matches(String value) {
            return code().equals(value);
        }
    }

    /** 预约幂等业务类型。 */
    private enum IdempotencyBusinessType {
        RESERVATION_CREATE;

        String code() {
            return name();
        }
    }

    /** 预约幂等执行结果。 */
    private enum IdempotencyResult {
        SUCCESS;

        String code() {
            return name();
        }
    }
}
