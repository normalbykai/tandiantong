package com.tandiantong.order.app;

import java.util.List;

public record OrderSkuSelection(Long skuId, int quantity, List<String> addonNames) {
}
