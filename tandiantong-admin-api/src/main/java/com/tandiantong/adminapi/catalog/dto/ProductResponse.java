package com.tandiantong.adminapi.catalog.dto;

import com.tandiantong.catalog.product.CatalogPersistenceService;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 商户商品响应。 */
@Schema(description = "商户商品响应")
public class ProductResponse {
    @Schema(description = "商品 ID", example = "1")
    private final Long productId;

    @Schema(description = "商品名称", example = "桂花拿铁")
    private final String productName;

    @Schema(description = "商品分类名称", example = "咖啡")
    private final String categoryName;

    @Schema(description = "商品状态", example = "ON_SHELF")
    private final String status;

    @Schema(description = "商品基础价格，单位为分", example = "1800")
    private final int basePriceCent;

    @Schema(description = "商品 SKU 列表")
    private final List<SkuResponse> skus;

    private ProductResponse(CatalogPersistenceService.AdminProduct product) {
        this.productId = product.productId();
        this.productName = product.productName();
        this.categoryName = product.categoryName();
        this.status = product.status();
        this.basePriceCent = product.basePriceCent();
        this.skus = product.skus().stream().map(SkuResponse::from).toList();
    }

    public static ProductResponse from(CatalogPersistenceService.AdminProduct product) {
        return new ProductResponse(product);
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getStatus() {
        return status;
    }

    public int getBasePriceCent() {
        return basePriceCent;
    }

    public List<SkuResponse> getSkus() {
        return skus;
    }

    /** 商户商品 SKU 响应。 */
    @Schema(description = "商户商品 SKU 响应")
    public static class SkuResponse {
        @Schema(description = "商品 SKU ID", example = "11")
        private final Long skuId;

        @Schema(description = "SKU 规格描述", example = "中杯热")
        private final String specificationText;

        @Schema(description = "商户自定义 SKU 编码", example = "GL-M-HOT")
        private final String skuCode;

        @Schema(description = "SKU 成交单价，单位为分", example = "1800")
        private final int priceCent;

        @Schema(description = "可售库存数量", example = "20")
        private final int availableStock;

        @Schema(description = "已锁定库存数量", example = "0")
        private final int lockedStock;

        @Schema(description = "库存预警阈值", example = "5")
        private final int warningStock;

        private SkuResponse(CatalogPersistenceService.AdminSku sku) {
            this.skuId = sku.skuId();
            this.specificationText = sku.specificationText();
            this.skuCode = sku.skuCode();
            this.priceCent = sku.priceCent();
            this.availableStock = sku.availableStock();
            this.lockedStock = sku.lockedStock();
            this.warningStock = sku.warningStock();
        }

        private static SkuResponse from(CatalogPersistenceService.AdminSku sku) {
            return new SkuResponse(sku);
        }

        public Long getSkuId() {
            return skuId;
        }

        public String getSpecificationText() {
            return specificationText;
        }

        public String getSkuCode() {
            return skuCode;
        }

        public int getPriceCent() {
            return priceCent;
        }

        public int getAvailableStock() {
            return availableStock;
        }

        public int getLockedStock() {
            return lockedStock;
        }

        public int getWarningStock() {
            return warningStock;
        }
    }
}
