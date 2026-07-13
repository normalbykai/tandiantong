package com.tandiantong.catalog.inventory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.catalog.entity.InventoryRecordEntity;
import com.tandiantong.catalog.entity.ProductSkuEntity;
import com.tandiantong.catalog.mapper.InventoryRecordMapper;
import com.tandiantong.catalog.mapper.ProductSkuMapper;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 库存应用服务，对外提供订单所需的定价、锁定、确认扣减和退款回补能力。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class InventoryApplicationService {

    private static final long SYSTEM_OPERATOR_ID = 0L;
    private static final String ORDER_LOCK_TYPE = "ORDER_LOCK";
    private static final String PAYMENT_DEDUCT_TYPE = "PAYMENT_DEDUCT";
    private static final String REFUND_RESTORE_TYPE = "REFUND_RESTORE";

    private final ProductSkuMapper productSkuMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    /**
     * 校验 SKU 可售状态、锁定库存并返回成交快照。
     */
    public PricedSku lockAndPrice(Long tenantId, Long storeId, Long skuId, int quantity, String orderNo) {
        Map<String, Object> row = productSkuMapper.selectOrderableSku(tenantId, storeId, skuId);
        if (row == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商品SKU不存在或已下架");
        }
        if (productSkuMapper.lockStock(tenantId, storeId, skuId, quantity) != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品库存不足");
        }
        ProductSkuEntity sku = requireSku(tenantId, storeId, skuId);
        insertRecord(sku, ORDER_LOCK_TYPE, quantity, orderNo, "订单锁定库存");
        int priceCent = ((Number) value(row, "price_cent")).intValue();
        return new PricedSku(skuId, String.valueOf(value(row, "product_name")),
                String.valueOf(value(row, "specification_text")), priceCent, quantity, priceCent * quantity);
    }

    /**
     * 支付成功后确认扣减锁定库存。
     */
    public void confirmPayment(Long tenantId, Long storeId, Long skuId, int quantity, String orderNo) {
        if (productSkuMapper.confirmLockedStock(tenantId, storeId, skuId, quantity) != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "订单锁定库存状态异常");
        }
        insertRecord(requireSku(tenantId, storeId, skuId), PAYMENT_DEDUCT_TYPE,
                quantity, orderNo, "支付确认扣减");
    }

    /**
     * 退款成功后回补可售库存。
     */
    public void restoreAfterRefund(Long tenantId, Long storeId, Long skuId, int quantity, String refundNo) {
        if (productSkuMapper.restoreAvailableStock(tenantId, storeId, skuId, quantity) != 1) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "退款商品SKU不存在");
        }
        insertRecord(requireSku(tenantId, storeId, skuId), REFUND_RESTORE_TYPE,
                quantity, refundNo, "退款回补库存");
    }

    private ProductSkuEntity requireSku(Long tenantId, Long storeId, Long skuId) {
        ProductSkuEntity sku = productSkuMapper.selectOne(Wrappers.<ProductSkuEntity>lambdaQuery()
                .eq(ProductSkuEntity::getId, skuId)
                .eq(ProductSkuEntity::getTenantId, tenantId)
                .eq(ProductSkuEntity::getStoreId, storeId));
        if (sku == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商品SKU不存在");
        }
        return sku;
    }

    private void insertRecord(ProductSkuEntity sku, String changeType, int quantity,
                              String businessNo, String reason) {
        InventoryRecordEntity record = new InventoryRecordEntity();
        record.setTenantId(sku.getTenantId());
        record.setStoreId(sku.getStoreId());
        record.setSkuId(sku.getId());
        record.setChangeType(changeType);
        record.setQuantity(quantity);
        record.setAvailableAfter(sku.getAvailableStock());
        record.setLockedAfter(sku.getLockedStock());
        record.setBusinessNo(businessNo);
        record.setReason(reason);
        record.setOperatorUserId(SYSTEM_OPERATOR_ID);
        inventoryRecordMapper.insert(record);
    }

    private Object value(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? row.get(key.toUpperCase()) : value;
    }

    /**
     * 下单时固化的 SKU 成交快照。
     */
    public record PricedSku(Long skuId, String productName, String specificationText,
                            int unitPriceCent, int quantity, int subtotalCent) {
    }
}
