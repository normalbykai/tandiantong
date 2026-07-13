package com.tandiantong.catalog.product;

import java.util.List;

/** 创建商品草稿命令。 */
public record ProductDraftCommand(
        String productName,
        String categoryName,
        int basePriceCent,
        PaymentConfigStatus paymentConfigStatus,
        boolean publishNow,
        List<SkuDraft> skus,
        List<AddonGroupDraft> addonGroups
) {
}
