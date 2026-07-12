package com.tandiantong.catalog.product;

import java.util.List;

public record AddonGroupDraft(
        String groupName,
        boolean required,
        int minSelect,
        int maxSelect,
        List<AddonOptionDraft> options
) {
}
