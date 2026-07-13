package com.tandiantong.catalog.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tandiantong.catalog.entity.InventoryRecordEntity;
import com.tandiantong.catalog.entity.ProductSkuEntity;
import com.tandiantong.catalog.mapper.InventoryRecordMapper;
import com.tandiantong.catalog.mapper.ProductSkuMapper;
import com.tandiantong.common.exception.BusinessException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * 库存应用服务测试，覆盖原子锁库存结果和库存流水快照。
 */
class InventoryApplicationServiceTest {

    private ProductSkuMapper productSkuMapper;
    private InventoryRecordMapper inventoryRecordMapper;
    private InventoryApplicationService service;

    @BeforeEach
    void setUp() {
        productSkuMapper = Mockito.mock(ProductSkuMapper.class);
        inventoryRecordMapper = Mockito.mock(InventoryRecordMapper.class);
        service = new InventoryApplicationService(productSkuMapper, inventoryRecordMapper);
    }

    @Test
    void shouldLockStockAndPersistSnapshotWithUppercaseAliases() {
        when(productSkuMapper.selectOrderableSku(1L, 2L, 3L)).thenReturn(Map.of(
                "PRODUCT_NAME", "鲜肉包", "SPECIFICATION_TEXT", "两个装", "PRICE_CENT", 600));
        when(productSkuMapper.lockStock(1L, 2L, 3L, 2)).thenReturn(1);
        ProductSkuEntity sku = new ProductSkuEntity();
        sku.setId(3L);
        sku.setTenantId(1L);
        sku.setStoreId(2L);
        sku.setAvailableStock(8);
        sku.setLockedStock(2);
        when(productSkuMapper.selectOne(any())).thenReturn(sku);

        InventoryApplicationService.PricedSku result = service.lockAndPrice(1L, 2L, 3L, 2, "SO1001");

        assertThat(result.subtotalCent()).isEqualTo(1200);
        ArgumentCaptor<InventoryRecordEntity> captor = ArgumentCaptor.forClass(InventoryRecordEntity.class);
        verify(inventoryRecordMapper).insert(captor.capture());
        assertThat(captor.getValue().getAvailableAfter()).isEqualTo(8);
        assertThat(captor.getValue().getLockedAfter()).isEqualTo(2);
        assertThat(captor.getValue().getBusinessNo()).isEqualTo("SO1001");
    }

    @Test
    void shouldRejectWhenAtomicStockLockFails() {
        when(productSkuMapper.selectOrderableSku(1L, 2L, 3L)).thenReturn(Map.of(
                "product_name", "鲜肉包", "specification_text", "两个装", "price_cent", 600));
        when(productSkuMapper.lockStock(1L, 2L, 3L, 9)).thenReturn(0);

        assertThatThrownBy(() -> service.lockAndPrice(1L, 2L, 3L, 9, "SO1002"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("商品库存不足");
    }
}
