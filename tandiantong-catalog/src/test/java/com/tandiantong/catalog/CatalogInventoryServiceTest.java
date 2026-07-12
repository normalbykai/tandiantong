package com.tandiantong.catalog;

import com.tandiantong.catalog.product.AddonGroupDraft;
import com.tandiantong.catalog.product.AddonOptionDraft;
import com.tandiantong.catalog.product.CatalogInventoryService;
import com.tandiantong.catalog.product.InventoryChangeType;
import com.tandiantong.catalog.product.PaymentConfigStatus;
import com.tandiantong.catalog.product.ProductDraftCommand;
import com.tandiantong.catalog.product.ProductStatus;
import com.tandiantong.catalog.product.SkuDraft;
import com.tandiantong.catalog.product.SpecificationValue;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogInventoryServiceTest {

    private final TenantStoreScope scope = new TenantStoreScope(1001L, 2001L, 3001L);

    @Test
    void shouldCreateDraftWhenPaidProductPaymentNotVerified() {
        CatalogInventoryService service = new CatalogInventoryService();
        ProductDraftCommand command = paidLatte(true, PaymentConfigStatus.NOT_CONFIGURED);

        var result = service.createProduct(scope, command);

        assertThat(result.product().status()).isEqualTo(ProductStatus.DRAFT);
        assertThat(result.skus()).hasSize(2);
        assertThat(result.skus().getFirst().availableStock()).isEqualTo(20);
        assertThat(result.inventoryRecords()).hasSize(2)
                .allSatisfy(record -> assertThat(record.changeType()).isEqualTo(InventoryChangeType.INITIAL_STOCK));
    }

    @Test
    void shouldRejectDuplicatedSkuCombination() {
        CatalogInventoryService service = new CatalogInventoryService();
        ProductDraftCommand command = new ProductDraftCommand("桂花拿铁", "咖啡", 1800,
                PaymentConfigStatus.VERIFIED, true,
                List.of(
                        new SkuDraft(List.of(new SpecificationValue("杯型", "中杯")), 1800, 10, 3, "GL-M-1"),
                        new SkuDraft(List.of(new SpecificationValue("杯型", "中杯")), 1900, 10, 3, "GL-M-2")
                ),
                List.of());

        assertThatThrownBy(() -> service.createProduct(scope, command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU 规格组合不能重复");
    }

    @Test
    void shouldValidateAddonSelectionRule() {
        CatalogInventoryService service = new CatalogInventoryService();
        ProductDraftCommand command = paidLatte(false, PaymentConfigStatus.VERIFIED);
        var result = service.createProduct(scope, command);

        assertThat(service.validateAddonSelection(result.product().productId(), "风味", List.of("燕麦奶")))
                .isTrue();
        assertThatThrownBy(() -> service.validateAddonSelection(result.product().productId(), "风味", List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("加料选择数量不符合要求");
        assertThatThrownBy(() -> service.validateAddonSelection(result.product().productId(), "风味",
                List.of("燕麦奶", "浓缩咖啡", "桂花冻")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("加料选择数量不符合要求");
    }

    @Test
    void shouldTrackInventoryLockReleaseDeductAndRestore() {
        CatalogInventoryService service = new CatalogInventoryService();
        var result = service.createProduct(scope, paidLatte(false, PaymentConfigStatus.VERIFIED));
        Long skuId = result.skus().getFirst().skuId();

        service.lockInventory(scope, skuId, 3, "ORDER-001");
        assertThat(service.findSku(scope, skuId).availableStock()).isEqualTo(17);
        assertThat(service.findSku(scope, skuId).lockedStock()).isEqualTo(3);

        service.releaseLockedInventory(scope, skuId, 1, "ORDER-001");
        assertThat(service.findSku(scope, skuId).availableStock()).isEqualTo(18);
        assertThat(service.findSku(scope, skuId).lockedStock()).isEqualTo(2);

        service.confirmPaymentDeduct(scope, skuId, 2, "PAY-001");
        assertThat(service.findSku(scope, skuId).availableStock()).isEqualTo(18);
        assertThat(service.findSku(scope, skuId).lockedStock()).isZero();

        service.restoreRefundedInventory(scope, skuId, 2, "REFUND-001");
        assertThat(service.findSku(scope, skuId).availableStock()).isEqualTo(20);
        assertThat(service.recordsOfSku(scope, skuId)).extracting("changeType")
                .contains(InventoryChangeType.ORDER_LOCK, InventoryChangeType.ORDER_RELEASE,
                        InventoryChangeType.PAYMENT_DEDUCT, InventoryChangeType.REFUND_RESTORE);
    }

    @Test
    void shouldRejectCrossTenantInventoryMutation() {
        CatalogInventoryService service = new CatalogInventoryService();
        var result = service.createProduct(scope, paidLatte(false, PaymentConfigStatus.VERIFIED));
        Long skuId = result.skus().getFirst().skuId();
        TenantStoreScope otherTenant = new TenantStoreScope(1002L, 2002L, 3002L);

        assertThatThrownBy(() -> service.lockInventory(otherTenant, skuId, 1, "ORDER-002"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商品资源不属于当前租户或门店");
    }

    @Test
    void shouldRecordManualInventoryAdjustment() {
        CatalogInventoryService service = new CatalogInventoryService();
        var result = service.createProduct(scope, paidLatte(false, PaymentConfigStatus.VERIFIED));
        Long skuId = result.skus().getFirst().skuId();

        service.adjustInventory(scope, skuId, InventoryChangeType.MANUAL_OUT, 4, "门店试饮消耗");
        service.adjustInventory(scope, skuId, InventoryChangeType.MANUAL_IN, 6, "上午补货入库");

        assertThat(service.findSku(scope, skuId).availableStock()).isEqualTo(22);
        assertThat(service.recordsOfSku(scope, skuId)).extracting("reason")
                .contains("门店试饮消耗", "上午补货入库");
    }

    private ProductDraftCommand paidLatte(boolean publishNow, PaymentConfigStatus paymentConfigStatus) {
        return new ProductDraftCommand("桂花拿铁", "咖啡", 1800, paymentConfigStatus, publishNow,
                List.of(
                        new SkuDraft(List.of(
                                new SpecificationValue("杯型", "中杯"),
                                new SpecificationValue("温度", "热")), 1800, 20, 5, "GL-M-HOT"),
                        new SkuDraft(List.of(
                                new SpecificationValue("杯型", "大杯"),
                                new SpecificationValue("温度", "冰")), 2200, 15, 5, "GL-L-ICE")
                ),
                List.of(new AddonGroupDraft("风味", true, 1, 2, List.of(
                        new AddonOptionDraft("燕麦奶", 300),
                        new AddonOptionDraft("浓缩咖啡", 400),
                        new AddonOptionDraft("桂花冻", 200)
                ))));
    }
}
