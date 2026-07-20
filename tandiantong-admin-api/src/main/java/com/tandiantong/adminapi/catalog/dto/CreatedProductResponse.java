package com.tandiantong.adminapi.catalog.dto;

import com.tandiantong.catalog.product.CatalogPersistenceService;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 商品创建响应。 */
@Schema(description = "商品创建响应")
public class CreatedProductResponse {
    @Schema(description = "商品 ID", example = "1")
    private final Long productId;

    @Schema(description = "商品名称", example = "招牌牛肉饭")
    private final String productName;

    @Schema(description = "商品分类名称", example = "主食")
    private final String categoryName;

    @Schema(description = "商品基础价格，单位为分", example = "1800")
    private final int basePriceCent;

    @Schema(description = "商品是否已上架", example = "true")
    private final boolean onShelf;

    @Schema(description = "创建的商品 SKU 列表")
    private final List<CreatedSkuResponse> skus;

    private CreatedProductResponse(CatalogPersistenceService.PersistedProduct product) {
        this.productId = product.productId();
        this.productName = product.productName();
        this.categoryName = product.categoryName();
        this.basePriceCent = product.basePriceCent();
        this.onShelf = product.onShelf();
        this.skus = product.skus().stream().map(CreatedSkuResponse::from).toList();
    }

    public static CreatedProductResponse from(CatalogPersistenceService.PersistedProduct product) {
        return new CreatedProductResponse(product);
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

    public int getBasePriceCent() {
        return basePriceCent;
    }

    public boolean isOnShelf() {
        return onShelf;
    }

    public List<CreatedSkuResponse> getSkus() {
        return skus;
    }

    @Schema(description = "创建的商品 SKU 响应")
    public static class CreatedSkuResponse {
        @Schema(description = "商品 SKU ID", example = "11")
        private final Long skuId;

        @Schema(description = "SKU 规格描述", example = "大份")
        private final String specificationText;

        @Schema(description = "商户自定义 SKU 编码", example = "BEEF-RICE-L")
        private final String skuCode;

        @Schema(description = "SKU 成交单价，单位为分", example = "2200")
        private final int priceCent;

        @Schema(description = "当前可售库存数量", example = "100")
        private final int availableStock;

        @Schema(description = "库存预警阈值", example = "10")
        private final int warningStock;

        private CreatedSkuResponse(CatalogPersistenceService.PersistedSku sku) {
            this.skuId = sku.skuId();
            this.specificationText = sku.specificationText();
            this.skuCode = sku.skuCode();
            this.priceCent = sku.priceCent();
            this.availableStock = sku.availableStock();
            this.warningStock = sku.warningStock();
        }

        private static CreatedSkuResponse from(CatalogPersistenceService.PersistedSku sku) {
            return new CreatedSkuResponse(sku);
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

        public int getWarningStock() {
            return warningStock;
        }
    }
}
