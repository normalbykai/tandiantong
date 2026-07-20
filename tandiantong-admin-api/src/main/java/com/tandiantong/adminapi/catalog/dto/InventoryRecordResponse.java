package com.tandiantong.adminapi.catalog.dto;

import com.tandiantong.catalog.product.CatalogPersistenceService;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** 库存流水响应。 */
@Schema(description = "库存流水响应")
public class InventoryRecordResponse {
    @Schema(description = "库存流水 ID", example = "100")
    private final Long id;

    @Schema(description = "库存变更时间", example = "2026-07-15T10:30:00")
    private final LocalDateTime createdAt;

    @Schema(description = "库存变更类型", example = "ORDER_LOCK")
    private final String changeType;

    @Schema(description = "商品名称快照", example = "桂花拿铁")
    private final String productName;

    @Schema(description = "SKU 规格描述快照", example = "中杯热")
    private final String specificationText;

    @Schema(description = "库存变更数量，增加为正数、减少为负数", example = "-2")
    private final int quantity;

    @Schema(description = "关联业务单号", example = "SO10001ABCDEF123456")
    private final String businessNo;

    @Schema(description = "库存变更原因", example = "创建订单锁定库存")
    private final String reason;

    private InventoryRecordResponse(CatalogPersistenceService.InventoryRecordView record) {
        this.id = record.id();
        this.createdAt = record.createdAt();
        this.changeType = record.changeType();
        this.productName = record.productName();
        this.specificationText = record.specificationText();
        this.quantity = record.quantity();
        this.businessNo = record.businessNo();
        this.reason = record.reason();
    }

    public static InventoryRecordResponse from(
            CatalogPersistenceService.InventoryRecordView record) {
        return new InventoryRecordResponse(record);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getProductName() {
        return productName;
    }

    public String getSpecificationText() {
        return specificationText;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public String getReason() {
        return reason;
    }
}
