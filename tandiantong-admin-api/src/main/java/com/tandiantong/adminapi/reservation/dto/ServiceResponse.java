package com.tandiantong.adminapi.reservation.dto;

import com.tandiantong.reservation.app.ReservationPersistenceService;
import io.swagger.v3.oas.annotations.media.Schema;

/** 预约服务创建响应。 */
@Schema(description = "预约服务创建响应")
public class ServiceResponse {
    @Schema(description = "服务项目 ID", example = "1") private final Long serviceId;
    @Schema(description = "服务项目名称", example = "肩颈放松") private final String name;
    @Schema(description = "支付模式", example = "FREE") private final String paymentMode;
    @Schema(description = "服务价格，单位为分", example = "0") private final int priceCent;
    @Schema(description = "服务时长，单位为分钟", example = "30") private final int durationMinutes;

    private ServiceResponse(ReservationPersistenceService.ServiceResult result) {
        this.serviceId = result.serviceId(); this.name = result.name(); this.paymentMode = result.paymentMode();
        this.priceCent = result.priceCent(); this.durationMinutes = result.durationMinutes();
    }
    public static ServiceResponse from(ReservationPersistenceService.ServiceResult result) { return new ServiceResponse(result); }
    public Long getServiceId() { return serviceId; }
    public String getName() { return name; }
    public String getPaymentMode() { return paymentMode; }
    public int getPriceCent() { return priceCent; }
    public int getDurationMinutes() { return durationMinutes; }
}
