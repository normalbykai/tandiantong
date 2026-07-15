package com.tandiantong.adminapi.analytics.dto;

import com.tandiantong.analytics.app.AnalyticsPersistenceService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 经营数据看板响应。 */
@Schema(description = "经营数据看板响应")
public class AnalyticsDashboardResponse {
    @Schema(description = "商品订单经营指标") private final OrderSummary order;
    @Schema(description = "预约经营指标") private final ReservationSummary reservation;
    @Schema(description = "商品销量排行") private final List<ProductMetric> products;
    private AnalyticsDashboardResponse(AnalyticsPersistenceService.Dashboard dashboard) {
        this.order = new OrderSummary(dashboard.order()); this.reservation = new ReservationSummary(dashboard.reservation());
        this.products = dashboard.products().stream().map(ProductMetric::new).toList();
    }
    public static AnalyticsDashboardResponse from(AnalyticsPersistenceService.Dashboard dashboard) { return new AnalyticsDashboardResponse(dashboard); }
    public OrderSummary getOrder() { return order; }
    public ReservationSummary getReservation() { return reservation; }
    public List<ProductMetric> getProducts() { return products; }

    @Schema(description = "商品订单经营指标")
    public static class OrderSummary {
        @Schema(description = "订单数量", example = "25") private final int orderCount;
        @Schema(description = "成交总额，单位为分", example = "45000") private final int grossCent;
        @Schema(description = "退款总额，单位为分", example = "3600") private final int refundCent;
        @Schema(description = "待核销订单数量", example = "3") private final int pendingVerification;
        private OrderSummary(AnalyticsPersistenceService.Summary summary) {
            this.orderCount = summary.orderCount(); this.grossCent = summary.grossCent();
            this.refundCent = summary.refundCent(); this.pendingVerification = summary.pendingVerification();
        }
        public int getOrderCount() { return orderCount; }
        public int getGrossCent() { return grossCent; }
        public int getRefundCent() { return refundCent; }
        public int getPendingVerification() { return pendingVerification; }
    }

    @Schema(description = "预约经营指标")
    public static class ReservationSummary {
        @Schema(description = "预约总数", example = "12") private final int total;
        @Schema(description = "已取消预约数", example = "2") private final int canceled;
        @Schema(description = "已履约预约数", example = "8") private final int fulfilled;
        private ReservationSummary(AnalyticsPersistenceService.ReservationMetrics metrics) {
            this.total = metrics.total(); this.canceled = metrics.canceled(); this.fulfilled = metrics.fulfilled();
        }
        public int getTotal() { return total; }
        public int getCanceled() { return canceled; }
        public int getFulfilled() { return fulfilled; }
    }

    @Schema(description = "商品销量排行指标")
    public static class ProductMetric {
        @Schema(description = "商品名称", example = "桂花拿铁") private final String name;
        @Schema(description = "销售数量", example = "18") private final int quantity;
        @Schema(description = "销售金额，单位为分", example = "32400") private final int amountCent;
        private ProductMetric(AnalyticsPersistenceService.ProductMetric metric) {
            this.name = metric.name(); this.quantity = metric.quantity(); this.amountCent = metric.amountCent();
        }
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public int getAmountCent() { return amountCent; }
    }
}
