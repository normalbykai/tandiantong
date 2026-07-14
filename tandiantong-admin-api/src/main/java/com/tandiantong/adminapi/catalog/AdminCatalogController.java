package com.tandiantong.adminapi.catalog;

import com.tandiantong.adminapi.catalog.dto.CreateProductRequest;
import com.tandiantong.adminapi.catalog.dto.CreateSkuRequest;
import com.tandiantong.catalog.product.CatalogPersistenceService;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 商户后台商品管理接口。 */
@RestController
@RequestMapping("/api/admin/v1/catalog/products")
@Tag(name = "商户商品")
public class AdminCatalogController {

    private final CatalogPersistenceService catalogPersistenceService;

    public AdminCatalogController(CatalogPersistenceService catalogPersistenceService) {
        this.catalogPersistenceService = catalogPersistenceService;
    }

    @Operation(summary = "创建商品和SKU")
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

    @Operation(summary = "查询商品列表")
    @GetMapping
    public List<CatalogPersistenceService.AdminProduct> list() { return catalogPersistenceService.listProducts(scope()); }

    private TenantStoreScope scope() { var user=SecurityContextHolder.currentUser(); return new TenantStoreScope(user.tenantId(),user.storeId(),user.userId()); }
}
