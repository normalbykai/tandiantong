package com.tandiantong.miniapi.catalog;

import com.tandiantong.catalog.product.CatalogPersistenceService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/mini/v1/catalog")
public class MiniCatalogController {

    private final CatalogPersistenceService catalogPersistenceService;

    public MiniCatalogController(CatalogPersistenceService catalogPersistenceService) {
        this.catalogPersistenceService = catalogPersistenceService;
    }

    @GetMapping("/products")
    public List<CatalogPersistenceService.MiniProduct> products(@RequestParam("scene") @NotBlank(message = "商户入口码不能为空") String scene) {
        return catalogPersistenceService.listOnShelfByScene(scene);
    }
}
