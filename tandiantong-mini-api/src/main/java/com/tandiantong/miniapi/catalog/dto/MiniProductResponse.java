package com.tandiantong.miniapi.catalog.dto;

import com.tandiantong.catalog.product.CatalogPersistenceService;

import io.swagger.v3.oas.annotations.media.Schema;

/** 小程序上架商品响应。 */
@Schema(description = "小程序上架商品响应")
public class MiniProductResponse {

    @Schema(description = "商品 ID", example = "1")
    private final Long productId;

    @Schema(description = "商品名称", example = "桂花拿铁")
    private final String productName;

    @Schema(description = "商品描述", example = "桂花香气与鲜奶融合")
    private final String description;

    @Schema(description = "商品展示价格，单位为分", example = "1800")
    private final int priceCent;

    @Schema(description = "商品分类名称", example = "咖啡")
    private final String categoryName;

    @Schema(description = "默认 SKU ID", example = "11")
    private final Long skuId;

    @Schema(description = "当前可售库存数量", example = "20")
    private final int availableStock;

    private MiniProductResponse(CatalogPersistenceService.MiniProduct product) {
        this.productId = product.productId();
        this.productName = product.productName();
        this.description = product.description();
        this.priceCent = product.priceCent();
        this.categoryName = product.categoryName();
        this.skuId = product.skuId();
        this.availableStock = product.availableStock();
    }

    public static MiniProductResponse from(CatalogPersistenceService.MiniProduct product) {
        return new MiniProductResponse(product);
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public int getPriceCent() {
        return priceCent;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Long getSkuId() {
        return skuId;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
