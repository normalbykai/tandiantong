package com.tandiantong.adminapi.verification.dto;

import com.tandiantong.verification.app.VerificationPersistenceService;
import io.swagger.v3.oas.annotations.media.Schema;

/** 核销处理响应。 */
@Schema(description = "核销处理响应")
public class VerificationResponse {
    @Schema(description = "被核销的业务单号", example = "SO10001ABCDEF123456") private final String businessNo;
    @Schema(description = "取餐号", example = "A001") private final String pickupNo;
    @Schema(description = "核销后的业务状态", example = "VERIFIED") private final String status;

    private VerificationResponse(VerificationPersistenceService.VerificationResult result) {
        this.businessNo = result.businessNo(); this.pickupNo = result.pickupNo(); this.status = result.status();
    }
    public static VerificationResponse from(VerificationPersistenceService.VerificationResult result) { return new VerificationResponse(result); }
    public String getBusinessNo() { return businessNo; }
    public String getPickupNo() { return pickupNo; }
    public String getStatus() { return status; }
}
