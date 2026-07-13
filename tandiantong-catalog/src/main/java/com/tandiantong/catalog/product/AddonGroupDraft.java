package com.tandiantong.catalog.product;

import java.util.List;

/** 加料分组草稿。 */
public record AddonGroupDraft(
        String groupName,
        boolean required,
        int minSelect,
        int maxSelect,
        List<AddonOptionDraft> options
) {
}
