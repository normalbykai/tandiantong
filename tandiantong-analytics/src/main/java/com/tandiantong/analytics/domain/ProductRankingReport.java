package com.tandiantong.analytics.domain;

import java.util.List;

public record ProductRankingReport(
        List<MetricRanking> productRankings,
        List<MetricRanking> skuRankings,
        List<MetricRanking> addonRankings
) {
}
