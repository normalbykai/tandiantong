package com.tandiantong.catalog.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.tandiantong.catalog.entity.AddonGroupEntity;
import com.tandiantong.catalog.entity.AddonOptionEntity;
import com.tandiantong.catalog.entity.ProductAddonRelationEntity;
import com.tandiantong.catalog.mapper.AddonGroupMapper;
import com.tandiantong.catalog.mapper.AddonOptionMapper;
import com.tandiantong.catalog.mapper.ProductAddonRelationMapper;
import com.tandiantong.framework.common.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * 持久化加料报价测试，覆盖下单时的加料金额快照和选择规则校验。
 */
class CatalogAddonPricingServiceTest {

    private ProductAddonRelationMapper productAddonRelationMapper;
    private AddonGroupMapper addonGroupMapper;
    private AddonOptionMapper addonOptionMapper;
    private CatalogAddonPricingService service;

    @BeforeEach
    void setUp() {
        productAddonRelationMapper = Mockito.mock(ProductAddonRelationMapper.class);
        addonGroupMapper = Mockito.mock(AddonGroupMapper.class);
        addonOptionMapper = Mockito.mock(AddonOptionMapper.class);
        service = new CatalogAddonPricingService(productAddonRelationMapper, addonGroupMapper, addonOptionMapper);
    }

    @Test
    void shouldQuoteEnabledAddonOptionsForProduct() {
        when(productAddonRelationMapper.selectList(any())).thenReturn(List.of(relation(10L)));
        when(addonGroupMapper.selectList(any())).thenReturn(List.of(group(10L, 0, 2)));
        when(addonOptionMapper.selectList(any())).thenReturn(List.of(option(10L, "加肉", 300), option(10L, "加蛋", 150)));

        CatalogAddonPricingService.AddonQuote quote = service.quoteAddonSelection(1L, 2L, 3L, List.of("加肉", "加蛋"));

        assertThat(quote.addonNames()).containsExactly("加肉", "加蛋");
        assertThat(quote.addonAmountCent()).isEqualTo(450);
    }

    @Test
    void shouldRejectWhenSelectedCountExceedsGroupRule() {
        when(productAddonRelationMapper.selectList(any())).thenReturn(List.of(relation(10L)));
        when(addonGroupMapper.selectList(any())).thenReturn(List.of(group(10L, 0, 1)));
        when(addonOptionMapper.selectList(any())).thenReturn(List.of(option(10L, "加肉", 300), option(10L, "加蛋", 150)));

        assertThatThrownBy(() -> service.quoteAddonSelection(1L, 2L, 3L, List.of("加肉", "加蛋")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("加料选择数量不符合要求");
    }

    private ProductAddonRelationEntity relation(Long groupId) {
        ProductAddonRelationEntity relation = new ProductAddonRelationEntity();
        relation.setAddonGroupId(groupId);
        return relation;
    }

    private AddonGroupEntity group(Long groupId, int minSelect, int maxSelect) {
        AddonGroupEntity group = new AddonGroupEntity();
        group.setId(groupId);
        group.setMinSelect(minSelect);
        group.setMaxSelect(maxSelect);
        return group;
    }

    private AddonOptionEntity option(Long groupId, String name, int priceCent) {
        AddonOptionEntity option = new AddonOptionEntity();
        option.setAddonGroupId(groupId);
        option.setName(name);
        option.setPriceCent(priceCent);
        return option;
    }
}
