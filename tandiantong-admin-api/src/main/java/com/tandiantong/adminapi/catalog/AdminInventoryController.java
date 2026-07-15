package com.tandiantong.adminapi.catalog;

import com.tandiantong.adminapi.catalog.dto.InventoryRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tandiantong.catalog.product.CatalogPersistenceService;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
import cn.dev33.satoken.annotation.SaCheckPermission;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商户后台库存流水查询接口。 */
@RestController
@RequestMapping("/api/admin/v1/catalog/inventory-records")
@Tag(name = "商户库存", description = "商户后台查询商品库存变更流水")
public class AdminInventoryController {
    private final CatalogPersistenceService catalogPersistenceService;

    public AdminInventoryController(CatalogPersistenceService catalogPersistenceService) {
        this.catalogPersistenceService = catalogPersistenceService;
    }

    @Operation(summary = "查询库存流水", description = "查询当前租户和门店下的库存增减记录及关联业务来源")
    @GetMapping
    @SaCheckPermission("catalog:inventory:read")
    public List<InventoryRecordResponse> list() {
        var user = SecurityContextHolder.currentUser();
        return catalogPersistenceService.listInventoryRecords(new TenantStoreScope(user.tenantId(), user.storeId(), user.userId()))
                .stream().map(InventoryRecordResponse::from).toList();
    }
}
