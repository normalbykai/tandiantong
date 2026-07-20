package com.tandiantong.analytics;

import com.tandiantong.analytics.app.AnalyticsService;
import com.tandiantong.analytics.app.ExportType;
import com.tandiantong.analytics.domain.AnalyticsOrderFact;
import com.tandiantong.analytics.domain.AnalyticsReservationFact;
import com.tandiantong.analytics.domain.OrderFactStatus;
import com.tandiantong.analytics.domain.ReservationFactStatus;
import com.tandiantong.analytics.tenant.TenantStoreScope;
import com.tandiantong.framework.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalyticsServiceTest {

    private final TenantStoreScope scope = new TenantStoreScope(1001L, 2001L, 3001L);
    private final LocalDate startDate = LocalDate.of(2026, 7, 1);
    private final LocalDate endDate = LocalDate.of(2026, 7, 12);

    @Test
    void shouldSummarizeTransactionByTenantAndDateRange() {
        AnalyticsService service = seededService();

        var summary = service.transactionSummary(scope, startDate, endDate);

        assertThat(summary.orderCount()).isEqualTo(3);
        assertThat(summary.paidOrderCount()).isEqualTo(2);
        assertThat(summary.grossAmountCent()).isEqualTo(7100);
        assertThat(summary.refundAmountCent()).isEqualTo(2200);
        assertThat(summary.netAmountCent()).isEqualTo(4900);
        assertThat(summary.pendingVerificationCount()).isEqualTo(1);
    }

    @Test
    void shouldRankProductsSkusAndAddonsWithinTenant() {
        AnalyticsService service = seededService();

        var ranking = service.productRanking(scope, startDate, endDate);

        assertThat(ranking.productRankings()).extracting("name").containsExactly("桂花拿铁", "手作三明治");
        assertThat(ranking.productRankings().getFirst().quantity()).isEqualTo(3);
        assertThat(ranking.skuRankings()).extracting("name").containsExactly("桂花拿铁 中杯热", "手作三明治 默认规格");
        assertThat(ranking.addonRankings()).extracting("name").containsExactly("燕麦奶", "浓缩咖啡");
    }

    @Test
    void shouldSummarizeReservationAndSlotUsage() {
        AnalyticsService service = seededService();

        var summary = service.reservationSummary(scope, startDate, endDate);

        assertThat(summary.reservationCount()).isEqualTo(3);
        assertThat(summary.canceledCount()).isEqualTo(1);
        assertThat(summary.fulfilledCount()).isEqualTo(1);
        assertThat(summary.averageSlotUsageRate()).isEqualByComparingTo("0.5000");
    }

    @Test
    void shouldRejectCrossTenantFactsAndExports() {
        AnalyticsService service = seededService();
        TenantStoreScope otherScope = new TenantStoreScope(1003L, 2003L, 3003L);

        assertThat(service.transactionSummary(otherScope, startDate, endDate).orderCount()).isZero();
        assertThatThrownBy(() -> service.createExportTask(otherScope, ExportType.TRANSACTION, startDate, endDate,
                "ops@example.test", true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("当前条件没有可导出的经营数据");
    }

    @Test
    void shouldCreateExportTaskWithMaskedOperatorAndAudit() {
        AnalyticsService service = seededService();

        var task = service.createExportTask(scope, ExportType.TRANSACTION, startDate, endDate,
                "zhangxiaochun@example.test", true);

        assertThat(task.fileName()).isEqualTo("经营数据-交易概览-20260701-20260712.xlsx");
        assertThat(task.operatorContact()).isEqualTo("z***@example.test");
        assertThat(task.status().name()).isEqualTo("FINISHED");
        assertThat(task.auditMessage()).contains("租户 1001").contains("脱敏");
        assertThat(service.exportTasks(scope)).containsExactly(task);
    }

    private AnalyticsService seededService() {
        AnalyticsService service = new AnalyticsService();
        service.recordOrder(new AnalyticsOrderFact(1001L, 2001L, "SO1001",
                LocalDate.of(2026, 7, 10), OrderFactStatus.PAID, 1800, 0, 1,
                "桂花拿铁", "桂花拿铁 中杯热", List.of("燕麦奶")));
        service.recordOrder(new AnalyticsOrderFact(1001L, 2001L, "SO1002",
                LocalDate.of(2026, 7, 10), OrderFactStatus.PENDING_VERIFY, 3100, 0, 2,
                "桂花拿铁", "桂花拿铁 中杯热", List.of("燕麦奶", "浓缩咖啡")));
        service.recordOrder(new AnalyticsOrderFact(1001L, 2001L, "SO1003",
                LocalDate.of(2026, 7, 11), OrderFactStatus.REFUNDED, 2200, 2200, 1,
                "手作三明治", "手作三明治 默认规格", List.of()));
        service.recordOrder(new AnalyticsOrderFact(1002L, 2002L, "SO2001",
                LocalDate.of(2026, 7, 10), OrderFactStatus.PAID, 9900, 0, 9,
                "其他租户商品", "其他规格", List.of("其他加料")));

        service.recordReservation(new AnalyticsReservationFact(1001L, 2001L, "YY1001",
                LocalDate.of(2026, 7, 10), ReservationFactStatus.FULFILLED, 10, 5));
        service.recordReservation(new AnalyticsReservationFact(1001L, 2001L, "YY1002",
                LocalDate.of(2026, 7, 10), ReservationFactStatus.CONFIRMED, 10, 5));
        service.recordReservation(new AnalyticsReservationFact(1001L, 2001L, "YY1003",
                LocalDate.of(2026, 7, 11), ReservationFactStatus.CANCELED, 8, 4));
        service.recordReservation(new AnalyticsReservationFact(1002L, 2002L, "YY2001",
                LocalDate.of(2026, 7, 10), ReservationFactStatus.FULFILLED, 1, 1));
        return service;
    }
}
