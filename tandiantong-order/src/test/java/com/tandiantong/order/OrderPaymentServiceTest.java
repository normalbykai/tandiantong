package com.tandiantong.order;

import com.tandiantong.catalog.product.CatalogInventoryService;
import com.tandiantong.catalog.product.InventoryChangeType;
import com.tandiantong.catalog.product.PaymentConfigStatus;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.integration.wechatpay.LocalWechatPayClient;
import com.tandiantong.order.app.CreateOrderCommand;
import com.tandiantong.order.app.OrderPaymentService;
import com.tandiantong.order.app.OrderSkuSelection;
import com.tandiantong.order.domain.OrderStatus;
import com.tandiantong.order.domain.RefundStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tandiantong.order.support.CatalogFixtures.paidLatte;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderPaymentServiceTest {

    private final TenantStoreScope scope = new TenantStoreScope(1001L, 2001L, 3001L);

    @Test
    void shouldCreateOrderWithSnapshotAndLockInventoryIdempotently() {
        CatalogInventoryService catalog = new CatalogInventoryService();
        var product = catalog.createProduct(scope, paidLatte(PaymentConfigStatus.VERIFIED));
        Long skuId = product.skus().getFirst().skuId();
        OrderPaymentService service = new OrderPaymentService(catalog, new LocalWechatPayClient("local-secret"));
        CreateOrderCommand command = command(skuId, "CREATE-001");

        var first = service.createOrder(scope, command);
        var repeated = service.createOrder(scope, command);

        assertThat(repeated.order().orderNo()).isEqualTo(first.order().orderNo());
        assertThat(first.order().status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(first.order().payAmountCent()).isEqualTo(4200);
        assertThat(first.items().getFirst().productName()).isEqualTo("桂花拿铁");
        assertThat(first.items().getFirst().skuText()).isEqualTo("杯型:中杯;温度:热");
        assertThat(first.items().getFirst().addonSnapshot()).contains("燕麦奶");
        assertThat(catalog.findSku(scope, skuId).availableStock()).isEqualTo(18);
        assertThat(catalog.findSku(scope, skuId).lockedStock()).isEqualTo(2);
    }

    @Test
    void shouldRejectInvalidWechatCallbackSignature() {
        CatalogInventoryService catalog = new CatalogInventoryService();
        var product = catalog.createProduct(scope, paidLatte(PaymentConfigStatus.VERIFIED));
        OrderPaymentService service = new OrderPaymentService(catalog, new LocalWechatPayClient("local-secret"));
        var created = service.createOrder(scope, command(product.skus().getFirst().skuId(), "CREATE-002"));

        assertThatThrownBy(() -> service.handlePaymentCallback(created.order().orderNo(), "TX-001", 4200, "bad"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("微信支付回调验签失败");
        assertThat(service.findOrder(scope, created.order().orderNo()).status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    void shouldConfirmPaymentOnceForRepeatedCallback() {
        CatalogInventoryService catalog = new CatalogInventoryService();
        var product = catalog.createProduct(scope, paidLatte(PaymentConfigStatus.VERIFIED));
        Long skuId = product.skus().getFirst().skuId();
        LocalWechatPayClient payClient = new LocalWechatPayClient("local-secret");
        OrderPaymentService service = new OrderPaymentService(catalog, payClient);
        var created = service.createOrder(scope, command(skuId, "CREATE-003"));
        String signature = payClient.signCallback(created.order().orderNo(), "TX-002", 4200);

        service.handlePaymentCallback(created.order().orderNo(), "TX-002", 4200, signature);
        service.handlePaymentCallback(created.order().orderNo(), "TX-002", 4200, signature);

        assertThat(service.findOrder(scope, created.order().orderNo()).status()).isEqualTo(OrderStatus.PENDING_VERIFY);
        assertThat(catalog.findSku(scope, skuId).availableStock()).isEqualTo(18);
        assertThat(catalog.findSku(scope, skuId).lockedStock()).isZero();
        assertThat(catalog.recordsOfSku(scope, skuId)).extracting("changeType")
                .containsOnlyOnce(InventoryChangeType.PAYMENT_DEDUCT);
    }

    @Test
    void shouldCancelPendingOrderAndReleaseInventory() {
        CatalogInventoryService catalog = new CatalogInventoryService();
        var product = catalog.createProduct(scope, paidLatte(PaymentConfigStatus.VERIFIED));
        Long skuId = product.skus().getFirst().skuId();
        OrderPaymentService service = new OrderPaymentService(catalog, new LocalWechatPayClient("local-secret"));
        var created = service.createOrder(scope, command(skuId, "CREATE-004"));

        service.cancelPendingOrder(scope, created.order().orderNo(), "顾客主动取消");

        assertThat(service.findOrder(scope, created.order().orderNo()).status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(catalog.findSku(scope, skuId).availableStock()).isEqualTo(20);
        assertThat(catalog.findSku(scope, skuId).lockedStock()).isZero();
    }

    @Test
    void shouldRefundPaidOrderBeforeVerificationIdempotentlyAndRestoreInventory() {
        CatalogInventoryService catalog = new CatalogInventoryService();
        var product = catalog.createProduct(scope, paidLatte(PaymentConfigStatus.VERIFIED));
        Long skuId = product.skus().getFirst().skuId();
        LocalWechatPayClient payClient = new LocalWechatPayClient("local-secret");
        OrderPaymentService service = new OrderPaymentService(catalog, payClient);
        var created = service.createOrder(scope, command(skuId, "CREATE-005"));
        String signature = payClient.signCallback(created.order().orderNo(), "TX-003", 4200);
        service.handlePaymentCallback(created.order().orderNo(), "TX-003", 4200, signature);

        var refund = service.refundWholeOrder(scope, created.order().orderNo(), "顾客临时有事", "REFUND-001");
        var repeated = service.refundWholeOrder(scope, created.order().orderNo(), "重复点击", "REFUND-001");

        assertThat(repeated.refundNo()).isEqualTo(refund.refundNo());
        assertThat(repeated.status()).isEqualTo(RefundStatus.SUCCESS);
        assertThat(service.findOrder(scope, created.order().orderNo()).status()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(catalog.findSku(scope, skuId).availableStock()).isEqualTo(20);
    }

    @Test
    void shouldRejectRefundAfterOrderCompleted() {
        CatalogInventoryService catalog = new CatalogInventoryService();
        var product = catalog.createProduct(scope, paidLatte(PaymentConfigStatus.VERIFIED));
        LocalWechatPayClient payClient = new LocalWechatPayClient("local-secret");
        OrderPaymentService service = new OrderPaymentService(catalog, payClient);
        var created = service.createOrder(scope, command(product.skus().getFirst().skuId(), "CREATE-006"));
        service.markCompletedForVerification(scope, created.order().orderNo());

        assertThatThrownBy(() -> service.refundWholeOrder(scope, created.order().orderNo(), "已完成后退款", "REFUND-002"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已完成订单不能在线退款");
    }

    private CreateOrderCommand command(Long skuId, String idempotencyKey) {
        return new CreateOrderCommand(idempotencyKey, "13800008000", "2026-07-12 18:30",
                List.of(new OrderSkuSelection(skuId, 2, List.of("燕麦奶"))));
    }
}
