package com.tandiantong.adminapi.order.dto;

import com.tandiantong.order.app.PersistentOrderService;
import io.swagger.v3.oas.annotations.media.Schema;

/** 订单整单退款响应。 */
@Schema(description = "订单整单退款响应")
public class RefundResponse {
    @Schema(description = "平台退款单号", example = "RF10001ABCDEF123456") private final String refundNo;
    @Schema(description = "退款状态", example = "SUCCESS") private final String status;
    @Schema(description = "退款金额，单位为分", example = "3600") private final int amountCent;

    private RefundResponse(PersistentOrderService.RefundResult result) {
        this.refundNo = result.refundNo(); this.status = result.status(); this.amountCent = result.amountCent();
    }
    public static RefundResponse from(PersistentOrderService.RefundResult result) { return new RefundResponse(result); }
    public String getRefundNo() { return refundNo; }
    public String getStatus() { return status; }
    public int getAmountCent() { return amountCent; }
}
