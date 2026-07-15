package com.tandiantong.miniapi.reservation.dto;

import com.tandiantong.reservation.app.ReservationPersistenceService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/** 小程序可预约服务响应。 */
@Schema(description = "小程序可预约服务响应")
public class MiniServiceResponse {
    @Schema(description = "服务项目 ID", example = "1") private final Long serviceId;
    @Schema(description = "服务项目名称", example = "肩颈放松") private final String name;
    @Schema(description = "支付模式，FREE 表示免费预约，PAID 表示付费预约", example = "FREE") private final String paymentMode;
    @Schema(description = "服务价格，单位为分", example = "0") private final int priceCent;
    @Schema(description = "服务时长，单位为分钟", example = "30") private final int durationMinutes;
    @Schema(description = "预约时段 ID", example = "10") private final Long slotId;
    @Schema(description = "服务日期", example = "2026-07-15") private final LocalDate serviceDate;
    @Schema(description = "时段开始时间", example = "09:00") private final String startTime;
    @Schema(description = "时段结束时间", example = "09:30") private final String endTime;
    @Schema(description = "剩余可预约容量", example = "4") private final int remainingCapacity;

    private MiniServiceResponse(ReservationPersistenceService.MiniService service) {
        this.serviceId = service.serviceId(); this.name = service.name(); this.paymentMode = service.paymentMode();
        this.priceCent = service.priceCent(); this.durationMinutes = service.durationMinutes(); this.slotId = service.slotId();
        this.serviceDate = service.serviceDate(); this.startTime = service.startTime(); this.endTime = service.endTime();
        this.remainingCapacity = service.remainingCapacity();
    }
    public static MiniServiceResponse from(ReservationPersistenceService.MiniService service) { return new MiniServiceResponse(service); }
    public Long getServiceId() { return serviceId; }
    public String getName() { return name; }
    public String getPaymentMode() { return paymentMode; }
    public int getPriceCent() { return priceCent; }
    public int getDurationMinutes() { return durationMinutes; }
    public Long getSlotId() { return slotId; }
    public LocalDate getServiceDate() { return serviceDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getRemainingCapacity() { return remainingCapacity; }
}
