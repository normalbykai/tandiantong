package com.tandiantong.order.app;

import java.util.List;

/** 下单时选择的 SKU、数量和加料。 */
public record OrderSkuSelection(Long skuId, int quantity, List<String> addonNames) {
}
