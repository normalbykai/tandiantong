package com.tandiantong.catalog.product;

import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** 商品、SKU、加料和库存领域服务。 */
@Service
public class CatalogInventoryService {

    private final AtomicLong idSequence = new AtomicLong(1000);
    private final Map<Long, ProductProfile> products = new LinkedHashMap<>();
    private final Map<Long, ProductSkuProfile> skus = new LinkedHashMap<>();
    private final Map<Long, List<AddonGroupProfile>> addonGroupsByProduct = new LinkedHashMap<>();
    private final List<InventoryRecord> inventoryRecords = new ArrayList<>();

    public ProductCreationResult createProduct(TenantStoreScope scope, ProductDraftCommand command) {
        validateProductDraft(command);
        Long productId = idSequence.incrementAndGet();
        ProductStatus status = resolveInitialStatus(command);
        ProductProfile product = new ProductProfile(productId, scope.tenantId(), scope.storeId(),
                command.productName(), command.categoryName(), command.basePriceCent(), status);
        products.put(productId, product);

        List<ProductSkuProfile> createdSkus = new ArrayList<>();
        for (SkuDraft skuDraft : command.skus()) {
            Long skuId = idSequence.incrementAndGet();
            ProductSkuProfile sku = new ProductSkuProfile(skuId, productId, scope.tenantId(), scope.storeId(),
                    specificationText(skuDraft.specifications()), skuDraft.priceCent(), skuDraft.initialStock(),
                    0, skuDraft.warningStock(), skuDraft.skuCode());
            skus.put(skuId, sku);
            createdSkus.add(sku);
            record(scope, skuId, InventoryChangeType.INITIAL_STOCK, skuDraft.initialStock(),
                    sku.availableStock(), sku.lockedStock(), "PRODUCT-" + productId, "商品初始库存");
        }

        List<AddonGroupProfile> addonGroups = createAddonGroups(productId, command.addonGroups());
        addonGroupsByProduct.put(productId, addonGroups);
        List<InventoryRecord> createdRecords = inventoryRecords.stream()
                .filter(record -> createdSkus.stream().anyMatch(sku -> sku.skuId().equals(record.skuId())))
                .toList();
        return new ProductCreationResult(product, List.copyOf(createdSkus), List.copyOf(addonGroups),
                List.copyOf(createdRecords));
    }

    public boolean validateAddonSelection(Long productId, String groupName, List<String> selectedOptionNames) {
        AddonGroupProfile group = addonGroupsByProduct.getOrDefault(productId, List.of()).stream()
                .filter(candidate -> candidate.groupName().equals(groupName))
                .findFirst()
                .orElseThrow(() -> businessError("加料分组不存在"));
        int selectedCount = selectedOptionNames == null ? 0 : selectedOptionNames.size();
        if (selectedCount < group.minSelect() || selectedCount > group.maxSelect()) {
            throw businessError("加料选择数量不符合要求");
        }
        List<String> enabledOptions = group.options().stream()
                .filter(AddonOptionProfile::enabled)
                .map(AddonOptionProfile::name)
                .toList();
        if (!enabledOptions.containsAll(selectedOptionNames)) {
            throw businessError("加料项不存在或已停用");
        }
        return true;
    }

    /**
     * 按商品可用加料规则校验并返回加料金额。
     */
    public AddonQuote quoteAddonSelection(Long productId, List<String> selectedOptionNames) {
        if (selectedOptionNames == null || selectedOptionNames.isEmpty()) {
            return new AddonQuote(List.of(), 0);
        }
        List<AddonGroupProfile> groups = addonGroupsByProduct.getOrDefault(productId, List.of());
        AddonGroupProfile matchedGroup = null;
        Map<String, Integer> matchedOptions = Map.of();
        for (AddonGroupProfile group : groups) {
            Map<String, Integer> groupOptions = group.options().stream()
                    .filter(AddonOptionProfile::enabled)
                    .collect(Collectors.toMap(AddonOptionProfile::name, AddonOptionProfile::priceCent));
            if (groupOptions.keySet().containsAll(selectedOptionNames)) {
                if (matchedGroup != null) {
                    throw businessError("加料项跨分组选择不合法");
                }
                matchedGroup = group;
                matchedOptions = groupOptions;
            }
        }
        if (matchedGroup == null) {
            throw businessError("加料项不存在或已停用");
        }
        int selectedCount = selectedOptionNames.size();
        if (selectedCount < matchedGroup.minSelect() || selectedCount > matchedGroup.maxSelect()) {
            throw businessError("加料选择数量不符合要求");
        }
        int amountCent = 0;
        for (String optionName : selectedOptionNames) {
            Integer priceCent = matchedOptions.get(optionName);
            if (priceCent == null) {
                throw businessError("加料项不存在或已停用");
            }
            amountCent += priceCent;
        }
        return new AddonQuote(List.copyOf(selectedOptionNames), amountCent);
    }

    public ProductSkuProfile findSku(TenantStoreScope scope, Long skuId) {
        ProductSkuProfile sku = skus.get(skuId);
        ensureSkuBelongsToScope(scope, sku);
        return sku;
    }

    /**
     * 按 SKU 查询商品资料，用于下单快照固化商品名称。
     */
    public ProductProfile findProductBySku(TenantStoreScope scope, Long skuId) {
        ProductSkuProfile sku = findSku(scope, skuId);
        ProductProfile product = products.get(sku.productId());
        if (product == null) {
            throw businessError("商品资源不存在");
        }
        ensureProductBelongsToScope(scope, product);
        return product;
    }

    public List<InventoryRecord> recordsOfSku(TenantStoreScope scope, Long skuId) {
        findSku(scope, skuId);
        return inventoryRecords.stream()
                .filter(record -> record.skuId().equals(skuId))
                .sorted(Comparator.comparing(InventoryRecord::createdAt))
                .toList();
    }

    public void adjustInventory(TenantStoreScope scope, Long skuId, InventoryChangeType changeType,
                                int quantity, String reason) {
        ProductSkuProfile sku = findSku(scope, skuId);
        if (changeType != InventoryChangeType.MANUAL_IN
                && changeType != InventoryChangeType.MANUAL_OUT
                && changeType != InventoryChangeType.STOCKTAKE) {
            throw businessError("手工库存调整类型不合法");
        }
        if (quantity <= 0) {
            throw businessError("库存调整数量必须大于零");
        }
        int availableAfter = switch (changeType) {
            case MANUAL_IN -> sku.availableStock() + quantity;
            case MANUAL_OUT -> sku.availableStock() - quantity;
            case STOCKTAKE -> quantity;
            default -> sku.availableStock();
        };
        if (availableAfter < 0) {
            throw businessError("可售库存不足");
        }
        updateStock(scope, sku, changeType, quantity, availableAfter, sku.lockedStock(), "MANUAL", reason);
    }

    public void lockInventory(TenantStoreScope scope, Long skuId, int quantity, String businessNo) {
        ProductSkuProfile sku = findSku(scope, skuId);
        validatePositiveQuantity(quantity);
        if (sku.availableStock() < quantity) {
            throw businessError("可售库存不足");
        }
        updateStock(scope, sku, InventoryChangeType.ORDER_LOCK, quantity,
                sku.availableStock() - quantity, sku.lockedStock() + quantity, businessNo, "订单锁定库存");
    }

    public void releaseLockedInventory(TenantStoreScope scope, Long skuId, int quantity, String businessNo) {
        ProductSkuProfile sku = findSku(scope, skuId);
        validatePositiveQuantity(quantity);
        if (sku.lockedStock() < quantity) {
            throw businessError("锁定库存不足");
        }
        updateStock(scope, sku, InventoryChangeType.ORDER_RELEASE, quantity,
                sku.availableStock() + quantity, sku.lockedStock() - quantity, businessNo, "订单释放库存");
    }

    public void confirmPaymentDeduct(TenantStoreScope scope, Long skuId, int quantity, String businessNo) {
        ProductSkuProfile sku = findSku(scope, skuId);
        validatePositiveQuantity(quantity);
        if (sku.lockedStock() < quantity) {
            throw businessError("锁定库存不足");
        }
        updateStock(scope, sku, InventoryChangeType.PAYMENT_DEDUCT, quantity,
                sku.availableStock(), sku.lockedStock() - quantity, businessNo, "支付确认扣减");
    }

    public void restoreRefundedInventory(TenantStoreScope scope, Long skuId, int quantity, String businessNo) {
        ProductSkuProfile sku = findSku(scope, skuId);
        validatePositiveQuantity(quantity);
        updateStock(scope, sku, InventoryChangeType.REFUND_RESTORE, quantity,
                sku.availableStock() + quantity, sku.lockedStock(), businessNo, "退款回补库存");
    }

    private void validateProductDraft(ProductDraftCommand command) {
        if (command.basePriceCent() < 0) {
            throw businessError("商品基础价格不能小于零");
        }
        if (command.skus() == null || command.skus().isEmpty()) {
            throw businessError("商品至少需要一个 SKU");
        }
        Map<String, Long> combinationCounts = command.skus().stream()
                .map(sku -> specificationText(sku.specifications()))
                .collect(Collectors.groupingBy(text -> text, Collectors.counting()));
        if (combinationCounts.values().stream().anyMatch(count -> count > 1)) {
            throw businessError("SKU 规格组合不能重复");
        }
        for (SkuDraft sku : command.skus()) {
            if (sku.priceCent() < 0) {
                throw businessError("SKU 价格不能小于零");
            }
            if (sku.initialStock() < 0 || sku.warningStock() < 0) {
                throw businessError("库存数量不能小于零");
            }
        }
        if (command.addonGroups() != null) {
            command.addonGroups().forEach(this::validateAddonGroup);
        }
    }

    private void validateAddonGroup(AddonGroupDraft group) {
        if (group.minSelect() < 0 || group.maxSelect() < group.minSelect()) {
            throw businessError("加料选择数量规则不合法");
        }
        if (group.required() && group.minSelect() == 0) {
            throw businessError("必选加料分组最少选择数量必须大于零");
        }
        if (group.options() == null || group.options().isEmpty()) {
            throw businessError("加料分组至少需要一个加料项");
        }
        for (AddonOptionDraft option : group.options()) {
            if (option.priceCent() < 0) {
                throw businessError("加料价格不能小于零");
            }
        }
    }

    private ProductStatus resolveInitialStatus(ProductDraftCommand command) {
        if (!command.publishNow()) {
            return ProductStatus.DRAFT;
        }
        if (command.basePriceCent() > 0 && command.paymentConfigStatus() != PaymentConfigStatus.VERIFIED) {
            return ProductStatus.DRAFT;
        }
        return ProductStatus.ON_SHELF;
    }

    private List<AddonGroupProfile> createAddonGroups(Long productId, List<AddonGroupDraft> drafts) {
        if (drafts == null) {
            return List.of();
        }
        List<AddonGroupProfile> groups = new ArrayList<>();
        for (AddonGroupDraft draft : drafts) {
            List<AddonOptionProfile> options = draft.options().stream()
                    .map(option -> new AddonOptionProfile(idSequence.incrementAndGet(), option.name(),
                            option.priceCent(), true))
                    .toList();
            groups.add(new AddonGroupProfile(idSequence.incrementAndGet(), productId, draft.groupName(),
                    draft.required(), draft.minSelect(), draft.maxSelect(), options));
        }
        return groups;
    }

    private void updateStock(TenantStoreScope scope, ProductSkuProfile sku, InventoryChangeType changeType,
                             int quantity, int availableAfter, int lockedAfter, String businessNo, String reason) {
        ProductSkuProfile changed = sku.withStock(availableAfter, lockedAfter);
        skus.put(sku.skuId(), changed);
        record(scope, sku.skuId(), changeType, quantity, availableAfter, lockedAfter, businessNo, reason);
    }

    private void record(TenantStoreScope scope, Long skuId, InventoryChangeType changeType, int quantity,
                        int availableAfter, int lockedAfter, String businessNo, String reason) {
        inventoryRecords.add(new InventoryRecord(idSequence.incrementAndGet(), scope.tenantId(), scope.storeId(),
                skuId, changeType, quantity, availableAfter, lockedAfter, businessNo, reason,
                scope.operatorUserId(), Instant.now()));
    }

    private void ensureSkuBelongsToScope(TenantStoreScope scope, ProductSkuProfile sku) {
        if (sku == null || !sku.tenantId().equals(scope.tenantId()) || !sku.storeId().equals(scope.storeId())) {
            throw businessError("商品资源不属于当前租户或门店");
        }
    }

    private void ensureProductBelongsToScope(TenantStoreScope scope, ProductProfile product) {
        if (product == null || !product.tenantId().equals(scope.tenantId()) || !product.storeId().equals(scope.storeId())) {
            throw businessError("商品资源不属于当前租户或门店");
        }
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw businessError("库存数量必须大于零");
        }
    }

    private String specificationText(List<SpecificationValue> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return "默认规格";
        }
        return specifications.stream()
                .map(specification -> specification.name() + ":" + specification.value())
                .collect(Collectors.joining(";"));
    }

    private BusinessException businessError(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    /** 加料报价结果。 */
    public record AddonQuote(List<String> addonNames, int addonAmountCent) {
    }
}
