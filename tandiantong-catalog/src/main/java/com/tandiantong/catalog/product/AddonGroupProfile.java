package com.tandiantong.catalog.product;

import java.util.List;

public record AddonGroupProfile(
        Long groupId,
        Long productId,
        String groupName,
        boolean required,
        int minSelect,
        int maxSelect,
        List<AddonOptionProfile> options
) {
}
