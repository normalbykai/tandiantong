package com.tandiantong.adminapi.catalog;

import com.tandiantong.catalog.product.CatalogPersistenceService;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/catalog/products")
public class AdminCatalogController {

    private final CatalogPersistenceService catalogPersistenceService;

    public AdminCatalogController(CatalogPersistenceService catalogPersistenceService) {
        this.catalogPersistenceService = catalogPersistenceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogPersistenceService.PersistedProduct create(@Valid @RequestBody CreateProductRequest request) {
        var currentUser = SecurityContextHolder.currentUser();
        TenantStoreScope scope = new TenantStoreScope(currentUser.tenantId(), currentUser.storeId(), currentUser.userId());
        return catalogPersistenceService.createProduct(scope, new CatalogPersistenceService.CreateCatalogProductCommand(
                request.productName(), request.categoryName(), request.basePriceCent(), request.onShelf(),
                request.skus().stream().map(sku -> new CatalogPersistenceService.CreateCatalogSkuCommand(
                        sku.specificationText(), sku.skuCode(), sku.priceCent(), sku.initialStock(), sku.warningStock())).toList()));
    }

    public record CreateProductRequest(
            @NotBlank(message = "商品名称不能为空") String productName,
            @NotBlank(message = "商品分类不能为空") String categoryName,
            @PositiveOrZero(message = "商品价格不能小于零") int basePriceCent,
            boolean onShelf,
            @NotEmpty(message = "至少需要一个SKU") List<CreateSkuRequest> skus
    ) {
    }

    public record CreateSkuRequest(
            @NotBlank(message = "SKU规格不能为空") String specificationText,
            @NotBlank(message = "SKU编码不能为空") String skuCode,
            @PositiveOrZero(message = "SKU价格不能小于零") int priceCent,
            @PositiveOrZero(message = "初始库存不能小于零") int initialStock,
            @PositiveOrZero(message = "预警库存不能小于零") int warningStock
    ) {
    }
}
