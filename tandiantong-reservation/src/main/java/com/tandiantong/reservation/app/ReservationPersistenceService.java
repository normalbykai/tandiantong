package com.tandiantong.reservation.app;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.integration.wechatpay.WechatPayClient;
import com.tandiantong.integration.wechatpay.WechatPrepayResult;
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
import com.tandiantong.verification.app.VerificationPersistenceService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private static final String RESERVATION_DESCRIPTION = "摊点通服务预约";

    private final ServiceItemMapper serviceItemMapper;
    private final ServiceSlotMapper serviceSlotMapper;
    private final ServiceReservationMapper serviceReservationMapper;
    private final ReservationIdempotencyRecordMapper idempotencyRecordMapper;
    private final MerchantSceneService merchantSceneService;
    private final WechatPayClient wechatPayClient;
    private final VerificationPersistenceService verificationPersistenceService;

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

        ServiceItemEntity serviceItem = serviceItemMapper.selectReservableService(scope.tenantId(), scope.storeId(),
                command.serviceId(), command.slotId(), ServiceStatus.ENABLED.code());
        if (serviceItem == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "预约服务或时段不存在");
        }
        if (serviceSlotMapper.occupyCapacity(scope.tenantId(), scope.storeId(), command.slotId()) != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约时段剩余名额不足");
        }

        String reservationNo = "YY" + scope.tenantId() + randomPart(14);
        ReservationStatus status = PaymentMode.FREE.matches(serviceItem.getPaymentMode())
                ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING_PAYMENT;
        WechatPrepayResult prepay = null;
        if (status == ReservationStatus.PENDING_PAYMENT) {
            prepay = wechatPayClient.createPrepay(reservationNo, serviceItem.getPriceCent(), RESERVATION_DESCRIPTION);
        }
        ServiceReservationEntity reservation = new ServiceReservationEntity();
        reservation.setTenantId(scope.tenantId());
        reservation.setStoreId(scope.storeId());
        reservation.setServiceId(command.serviceId());
        reservation.setSlotId(command.slotId());
        reservation.setReservationNo(reservationNo);
        reservation.setStatus(status.code());
        reservation.setContactMobile(command.contactMobile());
        reservation.setPayAmountCent(serviceItem.getPriceCent());
        reservation.setPrepayId(prepay == null ? null : prepay.prepayId());
        reservation.setExpireAt(status == ReservationStatus.PENDING_PAYMENT
                ? LocalDateTime.now(BUSINESS_ZONE_ID).plusMinutes(15) : null);
        serviceReservationMapper.insert(reservation);
        String verificationToken = null;
        String pickupNo = null;
        if (status == ReservationStatus.CONFIRMED) {
            VerificationPersistenceService.Credential credential = issueCredential(reservation);
            verificationToken = credential.token();
            pickupNo = credential.pickupNo();
            reservation.setVoucherCode(pickupNo);
        }
        insertIdempotency(scope.tenantId(), command.idempotencyKey(), reservationNo);
        return toResult(reservation, prepay == null ? "" : prepay.payNonce(), pickupNo, verificationToken);
    }

    /**
     * 处理预约微信支付回调，回调通过预约单号反查租户门店并执行金额与状态校验。
     */
    @Transactional
    public ReservationResult confirmPayment(String reservationNo, String transactionId, int amountCent, String signature) {
        ServiceReservationEntity reservation = serviceReservationMapper.selectByReservationNo(reservationNo);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "预约不存在");
        }
        if (!wechatPayClient.verifyCallback(reservationNo, transactionId, amountCent, signature)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "微信支付回调验签失败");
        }
        if (ReservationStatus.CONFIRMED.matches(reservation.getStatus())) {
            return toResult(reservation);
        }
        if (!ReservationStatus.PENDING_PAYMENT.matches(reservation.getStatus())
                || !Integer.valueOf(amountCent).equals(reservation.getPayAmountCent())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约支付状态或金额不正确");
        }
        int updated = serviceReservationMapper.confirmPayment(reservation.getTenantId(), reservation.getStoreId(),
                reservationNo, ReservationStatus.PENDING_PAYMENT.code(), ReservationStatus.CONFIRMED.code(),
                transactionId);
        if (updated == 0) {
            ServiceReservationEntity current = serviceReservationMapper.selectByReservationNo(reservationNo);
            return toResult(current);
        }
        reservation.setStatus(ReservationStatus.CONFIRMED.code());
        reservation.setTransactionId(transactionId);
        VerificationPersistenceService.Credential credential = issueCredential(reservation);
        reservation.setVoucherCode(credential.pickupNo());
        return toResult(reservation, "", credential.pickupNo(), credential.token());
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
                reservation.getPayAmountCent() == null ? 0 : reservation.getPayAmountCent(),
                reservation.getPrepayId(), "", reservation.getVoucherCode(), null);
    }

    /**
     * 取消指定时间前已过期的待支付预约，只有状态原子更新成功时才释放容量。
     */
    @Transactional
    public int cancelExpiredPendingReservations(Long tenantId, Long storeId, LocalDateTime expireBefore) {
        if (tenantId == null || storeId == null || expireBefore == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约超时取消参数不合法");
        }
        List<ServiceReservationEntity> candidates = serviceReservationMapper.selectExpiredPendingReservations(
                tenantId, storeId, ReservationStatus.PENDING_PAYMENT.code(), expireBefore);
        int canceledCount = 0;
        for (ServiceReservationEntity reservation : candidates) {
            int updated = serviceReservationMapper.cancelPendingPayment(tenantId, storeId,
                    reservation.getReservationNo(), ReservationStatus.PENDING_PAYMENT.code(),
                    ReservationStatus.CANCELED.code());
            if (updated == 1) {
                releaseCapacityAfterCancel(tenantId, storeId, reservation.getSlotId());
                canceledCount++;
            }
        }
        return canceledCount;
    }

    /**
     * 顾客按联系电话查询当前租户门店下的预约列表。
     */
    public List<CustomerReservationView> listCustomerReservations(Long tenantId, Long storeId,
                                                                  String contactMobile, String status) {
        validateCustomerBoundary(tenantId, storeId, contactMobile);
        return serviceReservationMapper.selectList(Wrappers.<ServiceReservationEntity>lambdaQuery()
                        .eq(ServiceReservationEntity::getTenantId, tenantId)
                        .eq(ServiceReservationEntity::getStoreId, storeId)
                        .eq(ServiceReservationEntity::getContactMobile, contactMobile)
                        .eq(status != null && !status.isBlank(), ServiceReservationEntity::getStatus, status)
                        .orderByDesc(ServiceReservationEntity::getCreatedAt))
                .stream()
                .map(this::toCustomerView)
                .toList();
    }

    /**
     * 顾客查询预约详情，必须同时匹配可信租户门店和联系电话。
     */
    public CustomerReservationView getCustomerReservationDetail(Long tenantId, Long storeId,
                                                               String reservationNo, String contactMobile) {
        validateCustomerBoundary(tenantId, storeId, contactMobile);
        if (reservationNo == null || reservationNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约单号不能为空");
        }
        ServiceReservationEntity reservation = serviceReservationMapper.selectOne(
                Wrappers.<ServiceReservationEntity>lambdaQuery()
                        .eq(ServiceReservationEntity::getTenantId, tenantId)
                        .eq(ServiceReservationEntity::getStoreId, storeId)
                        .eq(ServiceReservationEntity::getReservationNo, reservationNo)
                        .eq(ServiceReservationEntity::getContactMobile, contactMobile));
        if (reservation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "预约不存在");
        }
        return toCustomerView(reservation);
    }

    /**
     * 顾客取消本人预约，先校验联系电话边界，再复用统一取消流程释放容量。
     */
    @Transactional
    public ReservationResult cancelCustomerReservation(Long tenantId, Long storeId,
                                                       String reservationNo, String contactMobile) {
        getCustomerReservationDetail(tenantId, storeId, reservationNo, contactMobile);
        return cancel(new TenantStoreScope(tenantId, storeId, 0L), reservationNo);
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
        return toResult(reservation, "", reservation.getVoucherCode(), null);
    }

    private ReservationResult toResult(ServiceReservationEntity reservation, String paymentParameters,
                                       String pickupNo, String verificationToken) {
        String actualPickupNo = pickupNo == null ? reservation.getVoucherCode() : pickupNo;
        return new ReservationResult(reservation.getReservationNo(), reservation.getStatus(),
                reservation.getPayAmountCent() == null ? 0 : reservation.getPayAmountCent(),
                reservation.getPrepayId(), paymentParameters, actualPickupNo, verificationToken);
    }

    private VerificationPersistenceService.Credential issueCredential(ServiceReservationEntity reservation) {
        VerificationPersistenceService.Credential credential = verificationPersistenceService
                .issueReservationCredential(reservation.getTenantId(), reservation.getStoreId(),
                        reservation.getReservationNo(), "服务预约 " + reservation.getReservationNo());
        serviceReservationMapper.attachVoucher(reservation.getTenantId(), reservation.getStoreId(),
                reservation.getReservationNo(), credential.pickupNo());
        return credential;
    }

    private void releaseCapacityAfterCancel(Long tenantId, Long storeId, Long slotId) {
        if (serviceSlotMapper.releaseCapacity(tenantId, storeId, slotId) != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "预约容量状态异常");
        }
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

    private void validateCustomerBoundary(Long tenantId, Long storeId, String contactMobile) {
        if (tenantId == null || storeId == null
                || contactMobile == null || !contactMobile.matches("1\\d{10}")) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "顾客预约查询参数不合法");
        }
    }

    private String randomPart(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    private CustomerReservationView toCustomerView(ServiceReservationEntity reservation) {
        return new CustomerReservationView(reservation.getReservationNo(), reservation.getStatus(),
                reservation.getServiceId(), reservation.getSlotId(),
                reservation.getPayAmountCent() == null ? 0 : reservation.getPayAmountCent(),
                reservation.getContactMobile(), reservation.getVoucherCode(), reservation.getPrepayId(),
                reservation.getExpireAt(), reservation.getCreatedAt());
    }

    /** 创建服务项目命令。 */
    public static class CreateServiceCommand {
        private final String name;
        private final String paymentMode;
        private final int priceCent;
        private final int durationMinutes;

        public CreateServiceCommand(String name, String paymentMode, int priceCent, int durationMinutes) {
            this.name = name;
            this.paymentMode = paymentMode;
            this.priceCent = priceCent;
            this.durationMinutes = durationMinutes;
        }

        public String name() { return name; }
        public String paymentMode() { return paymentMode; }
        public int priceCent() { return priceCent; }
        public int durationMinutes() { return durationMinutes; }
    }

    /** 创建预约时段命令。 */
    public static class CreateSlotCommand {
        private final Long serviceId;
        private final LocalDate serviceDate;
        private final String startTime;
        private final String endTime;
        private final int capacity;

        public CreateSlotCommand(Long serviceId, LocalDate serviceDate, String startTime,
                                 String endTime, int capacity) {
            this.serviceId = serviceId;
            this.serviceDate = serviceDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.capacity = capacity;
        }

        public Long serviceId() { return serviceId; }
        public LocalDate serviceDate() { return serviceDate; }
        public String startTime() { return startTime; }
        public String endTime() { return endTime; }
        public int capacity() { return capacity; }
    }

    /** 顾客预约命令。 */
    public static class ReserveCommand {
        private final String idempotencyKey;
        private final Long serviceId;
        private final Long slotId;
        private final String contactMobile;

        public ReserveCommand(String idempotencyKey, Long serviceId, Long slotId, String contactMobile) {
            this.idempotencyKey = idempotencyKey;
            this.serviceId = serviceId;
            this.slotId = slotId;
            this.contactMobile = contactMobile;
        }

        public String idempotencyKey() { return idempotencyKey; }
        public Long serviceId() { return serviceId; }
        public Long slotId() { return slotId; }
        public String contactMobile() { return contactMobile; }
    }

    /** 服务项目处理结果。 */
    public static class ServiceResult {
        private final Long serviceId;
        private final String name;
        private final String paymentMode;
        private final int priceCent;
        private final int durationMinutes;

        public ServiceResult(Long serviceId, String name, String paymentMode,
                             int priceCent, int durationMinutes) {
            this.serviceId = serviceId;
            this.name = name;
            this.paymentMode = paymentMode;
            this.priceCent = priceCent;
            this.durationMinutes = durationMinutes;
        }

        public Long serviceId() { return serviceId; }
        public String name() { return name; }
        public String paymentMode() { return paymentMode; }
        public int priceCent() { return priceCent; }
        public int durationMinutes() { return durationMinutes; }
    }

    /** 预约时段处理结果。 */
    public static class SlotResult {
        private final Long slotId;
        private final Long serviceId;
        private final LocalDate serviceDate;
        private final String startTime;
        private final String endTime;
        private final int capacity;
        private final int usedCapacity;

        public SlotResult(Long slotId, Long serviceId, LocalDate serviceDate,
                          String startTime, String endTime, int capacity, int usedCapacity) {
            this.slotId = slotId;
            this.serviceId = serviceId;
            this.serviceDate = serviceDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.capacity = capacity;
            this.usedCapacity = usedCapacity;
        }

        public Long slotId() { return slotId; }
        public Long serviceId() { return serviceId; }
        public LocalDate serviceDate() { return serviceDate; }
        public String startTime() { return startTime; }
        public String endTime() { return endTime; }
        public int capacity() { return capacity; }
        public int usedCapacity() { return usedCapacity; }
    }

    /** 小程序可预约服务展示数据。 */
    public static class MiniService {
        private final Long serviceId;
        private final String name;
        private final String paymentMode;
        private final int priceCent;
        private final int durationMinutes;
        private final Long slotId;
        private final LocalDate serviceDate;
        private final String startTime;
        private final String endTime;
        private final int remainingCapacity;

        public MiniService(Long serviceId, String name, String paymentMode,
                           int priceCent, int durationMinutes, Long slotId,
                           LocalDate serviceDate, String startTime, String endTime,
                           int remainingCapacity) {
            this.serviceId = serviceId;
            this.name = name;
            this.paymentMode = paymentMode;
            this.priceCent = priceCent;
            this.durationMinutes = durationMinutes;
            this.slotId = slotId;
            this.serviceDate = serviceDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.remainingCapacity = remainingCapacity;
        }

        public Long serviceId() { return serviceId; }
        public String name() { return name; }
        public String paymentMode() { return paymentMode; }
        public int priceCent() { return priceCent; }
        public int durationMinutes() { return durationMinutes; }
        public Long slotId() { return slotId; }
        public LocalDate serviceDate() { return serviceDate; }
        public String startTime() { return startTime; }
        public String endTime() { return endTime; }
        public int remainingCapacity() { return remainingCapacity; }
    }

    /** 预约处理结果。 */
    public static class ReservationResult {
        private final String reservationNo;
        private final String status;
        private final int payAmountCent;
        private final String prepayId;
        private final String paymentParameters;
        private final String pickupNo;
        private final String verificationToken;

        public ReservationResult(String reservationNo, String status, int payAmountCent, String prepayId,
                                 String paymentParameters, String pickupNo, String verificationToken) {
            this.reservationNo = reservationNo;
            this.status = status;
            this.payAmountCent = payAmountCent;
            this.prepayId = prepayId;
            this.paymentParameters = paymentParameters;
            this.pickupNo = pickupNo;
            this.verificationToken = verificationToken;
        }

        public String reservationNo() { return reservationNo; }
        public String status() { return status; }
        public int payAmountCent() { return payAmountCent; }
        public String prepayId() { return prepayId; }
        public String paymentParameters() { return paymentParameters; }
        public String pickupNo() { return pickupNo; }
        public String verificationToken() { return verificationToken; }
    }

    /** 顾客预约展示视图。 */
    @Schema(description = "顾客预约展示视图")
    public static class CustomerReservationView {
        @Schema(description = "平台预约单号", example = "YY10001ABCDEF123456")
        private final String reservationNo;
        @Schema(description = "预约状态", example = "CONFIRMED")
        private final String status;
        @Schema(description = "服务项目 ID", example = "3")
        private final Long serviceId;
        @Schema(description = "预约时段 ID", example = "4")
        private final Long slotId;
        @Schema(description = "预约支付金额，单位为分", example = "9900")
        private final int payAmountCent;
        @Schema(description = "顾客联系电话", example = "13800000000")
        private final String contactMobile;
        @Schema(description = "预约取号，待支付时为空", example = "A001")
        private final String pickupNo;
        @Schema(description = "微信预支付标识，免费预约为空", example = "LOCAL-PREPAY-YY10001ABCDEF123456")
        private final String prepayId;
        @Schema(description = "待支付预约过期时间")
        private final LocalDateTime expireAt;
        @Schema(description = "预约创建时间")
        private final LocalDateTime createdAt;

        public CustomerReservationView(String reservationNo, String status, Long serviceId, Long slotId,
                                       int payAmountCent, String contactMobile, String pickupNo,
                                       String prepayId, LocalDateTime expireAt, LocalDateTime createdAt) {
            this.reservationNo = reservationNo;
            this.status = status;
            this.serviceId = serviceId;
            this.slotId = slotId;
            this.payAmountCent = payAmountCent;
            this.contactMobile = contactMobile;
            this.pickupNo = pickupNo;
            this.prepayId = prepayId;
            this.expireAt = expireAt;
            this.createdAt = createdAt;
        }

        public String getReservationNo() { return reservationNo; }
        public String getStatus() { return status; }
        public Long getServiceId() { return serviceId; }
        public Long getSlotId() { return slotId; }
        public int getPayAmountCent() { return payAmountCent; }
        public String getContactMobile() { return contactMobile; }
        public String getPickupNo() { return pickupNo; }
        public String getPrepayId() { return prepayId; }
        public LocalDateTime getExpireAt() { return expireAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    /** 服务启停状态。 */
    private enum ServiceStatus {
        /** 服务项目已启用，可对顾客展示和预约。 */
        ENABLED;

        String code() {
            return name();
        }
    }

    /** 服务支付模式。 */
    private enum PaymentMode {
        /** 免费预约，提交后直接确认。 */
        FREE,
        /** 付费预约，提交后等待支付。 */
        WECHAT_PAY,
        /** 历史测试数据使用的付费预约标识。 */
        PAID;

        static boolean supports(String value) {
            return FREE.matches(value) || WECHAT_PAY.matches(value) || PAID.matches(value);
        }

        boolean matches(String value) {
            return name().equals(value);
        }
    }

    /** 预约状态。 */
    private enum ReservationStatus {
        /** 付费预约已提交，等待支付。 */
        PENDING_PAYMENT,
        /** 预约已确认，等待履约。 */
        CONFIRMED,
        /** 预约已取消。 */
        CANCELED,
        /** 预约已完成履约。 */
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
        /** 创建预约幂等记录。 */
        RESERVATION_CREATE;

        String code() {
            return name();
        }
    }

    /** 预约幂等执行结果。 */
    private enum IdempotencyResult {
        /** 预约幂等操作执行成功。 */
        SUCCESS;

        String code() {
            return name();
        }
    }
}
