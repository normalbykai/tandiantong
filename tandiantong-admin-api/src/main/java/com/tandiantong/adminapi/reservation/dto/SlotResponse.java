package com.tandiantong.adminapi.reservation.dto;

import com.tandiantong.reservation.app.ReservationPersistenceService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/** 预约时段创建响应。 */
@Schema(description = "预约时段创建响应")
public class SlotResponse {
    @Schema(description = "预约时段 ID", example = "10") private final Long slotId;
    @Schema(description = "服务项目 ID", example = "1") private final Long serviceId;
    @Schema(description = "服务日期", example = "2026-07-15") private final LocalDate serviceDate;
    @Schema(description = "时段开始时间", example = "09:00") private final String startTime;
    @Schema(description = "时段结束时间", example = "09:30") private final String endTime;
    @Schema(description = "时段可预约容量", example = "5") private final int capacity;
    @Schema(description = "已占用预约容量", example = "0") private final int usedCapacity;

    private SlotResponse(ReservationPersistenceService.SlotResult result) {
        this.slotId = result.slotId(); this.serviceId = result.serviceId(); this.serviceDate = result.serviceDate();
        this.startTime = result.startTime(); this.endTime = result.endTime(); this.capacity = result.capacity();
        this.usedCapacity = result.usedCapacity();
    }
    public static SlotResponse from(ReservationPersistenceService.SlotResult result) { return new SlotResponse(result); }
    public Long getSlotId() { return slotId; }
    public Long getServiceId() { return serviceId; }
    public LocalDate getServiceDate() { return serviceDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getCapacity() { return capacity; }
    public int getUsedCapacity() { return usedCapacity; }
}
