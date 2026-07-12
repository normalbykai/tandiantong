package com.tandiantong.adminapi.catalog;

import com.tandiantong.catalog.product.CatalogPersistenceService;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/catalog/inventory-records")
public class AdminInventoryController {
    private final CatalogPersistenceService catalogPersistenceService;

    public AdminInventoryController(CatalogPersistenceService catalogPersistenceService) {
        this.catalogPersistenceService = catalogPersistenceService;
    }

    @GetMapping
    public List<CatalogPersistenceService.InventoryRecordView> list() {
        var user = SecurityContextHolder.currentUser();
        return catalogPersistenceService.listInventoryRecords(new TenantStoreScope(user.tenantId(), user.storeId(), user.userId()));
    }
}
