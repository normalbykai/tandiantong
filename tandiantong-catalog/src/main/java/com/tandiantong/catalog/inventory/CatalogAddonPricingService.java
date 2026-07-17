package com.tandiantong.catalog.inventory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.catalog.entity.AddonGroupEntity;
import com.tandiantong.catalog.entity.AddonOptionEntity;
import com.tandiantong.catalog.entity.ProductAddonRelationEntity;
import com.tandiantong.catalog.mapper.AddonGroupMapper;
import com.tandiantong.catalog.mapper.AddonOptionMapper;
import com.tandiantong.catalog.mapper.ProductAddonRelationMapper;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 持久化商品加料报价服务，用于下单时校验加料选择并固化加价金额。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class CatalogAddonPricingService {

    private final ProductAddonRelationMapper productAddonRelationMapper;
    private final AddonGroupMapper addonGroupMapper;
    private final AddonOptionMapper addonOptionMapper;

    public AddonQuote quoteAddonSelection(Long tenantId, Long storeId, Long productId, List<String> selectedOptionNames) {
        if (selectedOptionNames == null || selectedOptionNames.isEmpty()) {
            return new AddonQuote(List.of(), 0);
        }
        List<Long> groupIds = productAddonRelationMapper.selectList(Wrappers.<ProductAddonRelationEntity>lambdaQuery()
                        .eq(ProductAddonRelationEntity::getTenantId, tenantId)
                        .eq(ProductAddonRelationEntity::getStoreId, storeId)
                        .eq(ProductAddonRelationEntity::getProductId, productId))
                .stream()
                .map(ProductAddonRelationEntity::getAddonGroupId)
                .toList();
        if (groupIds.isEmpty()) {
            throw businessError("加料项不存在或已停用");
        }
        List<AddonGroupEntity> groups = addonGroupMapper.selectList(Wrappers.<AddonGroupEntity>lambdaQuery()
                .eq(AddonGroupEntity::getTenantId, tenantId)
                .eq(AddonGroupEntity::getStoreId, storeId)
                .eq(AddonGroupEntity::getEnabled, true)
                .in(AddonGroupEntity::getId, groupIds));
        AddonGroupEntity matchedGroup = null;
        Map<String, Integer> matchedOptions = Map.of();
        for (AddonGroupEntity group : groups) {
            Map<String, Integer> groupOptions = addonOptionMapper.selectList(Wrappers.<AddonOptionEntity>lambdaQuery()
                            .eq(AddonOptionEntity::getTenantId, tenantId)
                            .eq(AddonOptionEntity::getStoreId, storeId)
                            .eq(AddonOptionEntity::getAddonGroupId, group.getId())
                            .eq(AddonOptionEntity::getEnabled, true))
                    .stream()
                    .collect(Collectors.toMap(AddonOptionEntity::getName, AddonOptionEntity::getPriceCent));
            if (groupOptions.keySet().containsAll(selectedOptionNames)) {
                if (matchedGroup != null) {
                    throw businessError("加料项跨分组选择不合法");
                }
                matchedGroup = group;
                matchedOptions = groupOptions;
            }
        }
        if (matchedGroup == null) {
            throw businessError("加料项不存在或已停用");
        }
        int selectedCount = selectedOptionNames.size();
        if (selectedCount < matchedGroup.getMinSelect() || selectedCount > matchedGroup.getMaxSelect()) {
            throw businessError("加料选择数量不符合要求");
        }
        int amountCent = 0;
        for (String optionName : selectedOptionNames) {
            amountCent += matchedOptions.get(optionName);
        }
        return new AddonQuote(List.copyOf(selectedOptionNames), amountCent);
    }

    private BusinessException businessError(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    /**
     * 加料报价结果。
     */
    public static class AddonQuote {

        private final List<String> addonNames;
        private final int addonAmountCent;

        public AddonQuote(List<String> addonNames, int addonAmountCent) {
            this.addonNames = addonNames;
            this.addonAmountCent = addonAmountCent;
        }

        public List<String> addonNames() {
            return addonNames;
        }

        public int addonAmountCent() {
            return addonAmountCent;
        }
    }
}
