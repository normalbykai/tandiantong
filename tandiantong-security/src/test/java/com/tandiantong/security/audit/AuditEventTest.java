package com.tandiantong.security.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuditEventTest {

    @Test
    void shouldRenderMerchantDisableDetailFromStructuredEvent() {
        AuditEvent event = AuditEvent.of(
                AuditAction.MERCHANT_DISABLED,
                AuditTarget.of("商户租户", 123L, "春和小吃铺"));

        assertThat(event.renderDetail())
                .isEqualTo("已停用商户：春和小吃铺；商户后台将无法登录，且不能发起新的业务操作");
    }

    @Test
    void shouldRenderOrderRefundDetailFromOrderNumber() {
        AuditEvent event = AuditEvent.of(
                AuditAction.ORDER_REFUND_REQUESTED,
                AuditTarget.of("商品订单", "SO202607220001"));

        assertThat(event.renderDetail())
                .isEqualTo("已提交商品订单：SO202607220001的核销前整单退款申请");
    }
}
