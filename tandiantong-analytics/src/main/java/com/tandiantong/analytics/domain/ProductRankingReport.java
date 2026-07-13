package com.tandiantong.analytics.domain;

import java.util.List;

/** 商品销售排行报告。 */
public record ProductRankingReport(
        List<MetricRanking> productRankings,
        List<MetricRanking> skuRankings,
        List<MetricRanking> addonRankings
) {
}
