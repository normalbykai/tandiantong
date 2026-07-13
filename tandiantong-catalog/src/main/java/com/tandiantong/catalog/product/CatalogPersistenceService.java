package com.tandiantong.catalog.product;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tandiantong.catalog.entity.InventoryRecordEntity;
import com.tandiantong.catalog.entity.ProductCategoryEntity;
import com.tandiantong.catalog.entity.ProductEntity;
import com.tandiantong.catalog.entity.ProductSkuEntity;
import com.tandiantong.catalog.mapper.InventoryRecordMapper;
import com.tandiantong.catalog.mapper.ProductCategoryMapper;
import com.tandiantong.catalog.mapper.ProductMapper;
import com.tandiantong.catalog.mapper.ProductSkuMapper;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品库存持久化服务，负责商品、SKU 和库存流水的数据库读写。
 */
@Service
public class CatalogPersistenceService {

    private static final String DEFAULT_CATEGORY_NAME = "推荐";
    private static final String DEFAULT_SPECIFICATION_TEXT = "默认规格";
    private static final String PRODUCT_BUSINESS_NO_PREFIX = "PRODUCT-";
    private static final String INITIAL_STOCK_REASON = "商品初始库存";

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    public CatalogPersistenceService(ProductCategoryMapper productCategoryMapper, ProductMapper productMapper,
                                     ProductSkuMapper productSkuMapper, InventoryRecordMapper inventoryRecordMapper) {
        this.productCategoryMapper = productCategoryMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
    }

    /**
     * 创建商品、SKU 和初始库存流水。
     */
    @Transactional
    public PersistedProduct createProduct(TenantStoreScope scope, CreateCatalogProductCommand command) {
        validate(command);
        Long categoryId = findOrCreateCategory(scope, command.categoryName());
        ProductEntity product = new ProductEntity();
        product.setTenantId(scope.tenantId());
        product.setStoreId(scope.storeId());
        product.setCategoryId(categoryId);
        product.setName(command.productName());
        product.setBasePriceCent(command.basePriceCent());
        product.setStatus(command.onShelf() ? ProductStatus.ON_SHELF.name() : ProductStatus.DRAFT.name());
        productMapper.insert(product);
        Long productId = product.getId();
        List<PersistedSku> skus = command.skus().stream().map(sku -> createSku(scope, productId, sku)).toList();
        return new PersistedProduct(productId, command.productName(), command.categoryName(), command.basePriceCent(), command.onShelf(), skus);
    }

    /**
     * 按小程序入口码查询 C 端上架商品。
     */
    public List<MiniProduct> listOnShelfByScene(String sceneKey) {
        return productMapper.selectMiniProductsByScene(sceneKey).stream()
                .map(row -> new MiniProduct(longValue(row, "product_id"), stringValue(row, "product_name"),
                        stringValue(row, "description"), intValue(row, "price_cent"),
                        stringValue(row, "category_name"), longValue(row, "sku_id"), intValue(row, "available_stock")))
                .toList();
    }

    /**
     * 查询后台商品列表。
     */
    public List<AdminProduct> listProducts(TenantStoreScope scope) {
        return productMapper.selectList(new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getTenantId, scope.tenantId())
                        .eq(ProductEntity::getStoreId, scope.storeId())
                        .orderByDesc(ProductEntity::getId)
                        .last("limit 200"))
                .stream()
                .map(product -> new AdminProduct(product.getId(), product.getName(), categoryName(product),
                        product.getStatus(), product.getBasePriceCent(), listSkus(scope, product.getId())))
                .toList();
    }

    /**
     * 查询后台库存流水。
     */
    public List<InventoryRecordView> listInventoryRecords(TenantStoreScope scope) {
        return inventoryRecordMapper.selectInventoryRecordViews(scope.tenantId(), scope.storeId()).stream()
                .map(row -> new InventoryRecordView(longValue(row, "id"), dateTimeValue(row, "created_at"),
                        stringValue(row, "change_type"), stringValue(row, "product_name"),
                        stringValue(row, "specification_text"), intValue(row, "quantity"),
                        stringValue(row, "business_no"), stringValue(row, "reason")))
                .toList();
    }

    private List<AdminSku> listSkus(TenantStoreScope scope, Long productId) {
        return productSkuMapper.selectList(new LambdaQueryWrapper<ProductSkuEntity>()
                        .eq(ProductSkuEntity::getTenantId, scope.tenantId())
                        .eq(ProductSkuEntity::getStoreId, scope.storeId())
                        .eq(ProductSkuEntity::getProductId, productId)
                        .orderByAsc(ProductSkuEntity::getId))
                .stream()
                .map(sku -> new AdminSku(sku.getId(), sku.getSpecificationText(), sku.getSkuCode(), sku.getPriceCent(),
                        sku.getAvailableStock(), sku.getLockedStock(), sku.getWarningStock()))
                .toList();
    }

    private PersistedSku createSku(TenantStoreScope scope, Long productId, CreateCatalogSkuCommand sku) {
        ProductSkuEntity skuEntity = new ProductSkuEntity();
        skuEntity.setTenantId(scope.tenantId());
        skuEntity.setStoreId(scope.storeId());
        skuEntity.setProductId(productId);
        skuEntity.setSpecificationText(normalizeSpecificationText(sku.specificationText()));
        skuEntity.setSkuCode(sku.skuCode());
        skuEntity.setPriceCent(sku.priceCent());
        skuEntity.setAvailableStock(sku.initialStock());
        skuEntity.setLockedStock(0);
        skuEntity.setWarningStock(sku.warningStock());
        skuEntity.setEnabled(true);
        productSkuMapper.insert(skuEntity);

        InventoryRecordEntity record = new InventoryRecordEntity();
        record.setTenantId(scope.tenantId());
        record.setStoreId(scope.storeId());
        record.setSkuId(skuEntity.getId());
        record.setChangeType(InventoryChangeType.INITIAL_STOCK.name());
        record.setQuantity(sku.initialStock());
        record.setAvailableAfter(sku.initialStock());
        record.setLockedAfter(0);
        record.setBusinessNo(PRODUCT_BUSINESS_NO_PREFIX + productId);
        record.setReason(INITIAL_STOCK_REASON);
        record.setOperatorUserId(scope.operatorUserId());
        inventoryRecordMapper.insert(record);
        return new PersistedSku(skuEntity.getId(), skuEntity.getSpecificationText(), sku.skuCode(), sku.priceCent(),
                sku.initialStock(), sku.warningStock());
    }

    private Long findOrCreateCategory(TenantStoreScope scope, String categoryName) {
        ProductCategoryEntity existing = productCategoryMapper.selectOne(new LambdaQueryWrapper<ProductCategoryEntity>()
                .eq(ProductCategoryEntity::getTenantId, scope.tenantId())
                .eq(ProductCategoryEntity::getStoreId, scope.storeId())
                .eq(ProductCategoryEntity::getName, categoryName));
        if (existing != null) {
            return existing.getId();
        }
        ProductCategoryEntity category = new ProductCategoryEntity();
        category.setTenantId(scope.tenantId());
        category.setStoreId(scope.storeId());
        category.setName(categoryName);
        category.setSortOrder(0);
        category.setEnabled(true);
        productCategoryMapper.insert(category);
        return category.getId();
    }

    private String categoryName(ProductEntity product) {
        if (product.getCategoryId() == null) {
            return DEFAULT_CATEGORY_NAME;
        }
        ProductCategoryEntity category = productCategoryMapper.selectOne(new LambdaQueryWrapper<ProductCategoryEntity>()
                .eq(ProductCategoryEntity::getId, product.getCategoryId())
                .eq(ProductCategoryEntity::getTenantId, product.getTenantId())
                .eq(ProductCategoryEntity::getStoreId, product.getStoreId()));
        return category == null ? DEFAULT_CATEGORY_NAME : category.getName();
    }

    private void validate(CreateCatalogProductCommand command) {
        if (command.productName() == null || command.productName().isBlank() || command.categoryName() == null || command.categoryName().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品名称和分类不能为空");
        }
        if (command.basePriceCent() < 0 || command.skus() == null || command.skus().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品价格或SKU配置不合法");
        }
        for (CreateCatalogSkuCommand sku : command.skus()) {
            if (sku.priceCent() < 0 || sku.initialStock() < 0 || sku.warningStock() < 0 || sku.skuCode() == null || sku.skuCode().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "SKU价格、库存或编码不合法");
            }
        }
    }

    private String normalizeSpecificationText(String specificationText) {
        return specificationText == null || specificationText.isBlank() ? DEFAULT_SPECIFICATION_TEXT : specificationText;
    }

    private Long longValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase());
        }
        return value == null ? null : ((Number) value).longValue();
    }

    private int intValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase());
        }
        return value == null ? 0 : ((Number) value).intValue();
    }

    private String stringValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase());
        }
        return value == null ? null : value.toString();
    }

    private LocalDateTime dateTimeValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase());
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    public record CreateCatalogProductCommand(String productName, String categoryName, int basePriceCent, boolean onShelf,
                                              List<CreateCatalogSkuCommand> skus) {
    }

    public record CreateCatalogSkuCommand(String specificationText, String skuCode, int priceCent, int initialStock,
                                          int warningStock) {
    }

    public record PersistedProduct(Long productId, String productName, String categoryName, int basePriceCent, boolean onShelf,
                                   List<PersistedSku> skus) {
    }

    public record PersistedSku(Long skuId, String specificationText, String skuCode, int priceCent, int availableStock,
                               int warningStock) {
    }

    public record MiniProduct(Long productId, String productName, String description, int priceCent, String categoryName,Long skuId,int availableStock) {
    }
    public record AdminProduct(Long productId,String productName,String categoryName,String status,int basePriceCent,List<AdminSku> skus) {}
    public record AdminSku(Long skuId,String specificationText,String skuCode,int priceCent,int availableStock,int lockedStock,int warningStock) {}
    public record InventoryRecordView(Long id,java.time.LocalDateTime createdAt,String changeType,String productName,String specificationText,int quantity,String businessNo,String reason) {}
}
