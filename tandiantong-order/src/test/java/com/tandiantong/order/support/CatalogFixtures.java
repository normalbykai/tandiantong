package com.tandiantong.order.support;

import com.tandiantong.catalog.product.AddonGroupDraft;
import com.tandiantong.catalog.product.AddonOptionDraft;
import com.tandiantong.catalog.product.PaymentConfigStatus;
import com.tandiantong.catalog.product.ProductDraftCommand;
import com.tandiantong.catalog.product.SkuDraft;
import com.tandiantong.catalog.product.SpecificationValue;

import java.util.List;

public final class CatalogFixtures {

    private CatalogFixtures() {
    }

    public static ProductDraftCommand paidLatte(PaymentConfigStatus paymentConfigStatus) {
        return new ProductDraftCommand("桂花拿铁", "咖啡", 1800, paymentConfigStatus, true,
                List.of(new SkuDraft(List.of(
                        new SpecificationValue("杯型", "中杯"),
                        new SpecificationValue("温度", "热")), 1800, 20, 5, "GL-M-HOT")),
                List.of(new AddonGroupDraft("风味", false, 0, 2, List.of(
                        new AddonOptionDraft("燕麦奶", 300),
                        new AddonOptionDraft("浓缩咖啡", 400)
                ))));
    }
}
