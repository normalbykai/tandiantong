package com.tandiantong.miniapi.catalog;

import com.tandiantong.miniapi.catalog.dto.MiniProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tandiantong.catalog.product.CatalogPersistenceService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 小程序商品浏览接口。 */
@Validated
@RestController
@RequestMapping("/api/mini/v1/catalog")
@Tag(name = "小程序商品", description = "顾客通过商户入口码查询当前可售商品")
public class MiniCatalogController {

    private final CatalogPersistenceService catalogPersistenceService;

    public MiniCatalogController(CatalogPersistenceService catalogPersistenceService) {
        this.catalogPersistenceService = catalogPersistenceService;
    }

    @Operation(summary = "查询上架商品", description = "查询指定商户入口下已上架且可在小程序展示的商品")
    @GetMapping("/products")
    public List<MiniProductResponse> products(
            @Parameter(description = "商户小程序入口码", example = "scene_xinghe_001", required = true)
            @RequestParam("scene") @NotBlank(message = "商户入口码不能为空") String scene) {
        return catalogPersistenceService.listOnShelfByScene(scene).stream().map(MiniProductResponse::from).toList();
    }
}
